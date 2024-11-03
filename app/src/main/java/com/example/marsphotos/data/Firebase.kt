package com.example.marsphotos.data

import android.util.Log
import com.example.marsphotos.model.MarsPhoto
import com.example.marsphotos.model.PicsumPhoto
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

const val TAG = "FirebaseStore"

const val COLLECTION = "saved_images"

const val APP_PICTURES_DOCUMENT = "latest_pictures"
const val ROLL_COUNT_DOCUMENT = "roll_count"
const val CAMERA_PICTURE_DOCUMENT = "camera_photo"

class Firebase {
    private val firebase = Firebase.firestore

    fun saveAppPictures(marsPhoto: MarsPhoto, picsumPhoto: PicsumPhoto) {
        val map = hashMapOf(
            "mars" to Json.encodeToString(marsPhoto),
            "picsum" to Json.encodeToString(picsumPhoto)
        )
        save(APP_PICTURES_DOCUMENT, map)
    }

    fun saveCameraPic(cameraPic: String) {
        val map = hashMapOf(
            "camera_pic" to cameraPic,
        )
        save(CAMERA_PICTURE_DOCUMENT, map)
    }

    fun incrementRollCount(callback: (Int) -> Unit) {
        val documentRef = firebase.collection(COLLECTION).document(ROLL_COUNT_DOCUMENT)
        documentRef.get()
            .addOnSuccessListener { document ->
                if (document.exists() && document.contains(ROLL_COUNT_DOCUMENT)) {
                    documentRef.update(ROLL_COUNT_DOCUMENT, FieldValue.increment(1))
                        .addOnSuccessListener {
                            documentRef.get()
                                .addOnSuccessListener { updatedDocument ->
                                    val newCount = updatedDocument.getLong(ROLL_COUNT_DOCUMENT) ?: 0
                                    Log.d(TAG, "Roll count incremented to $newCount.")
                                    callback(newCount.toInt())
                                }
                                .addOnFailureListener { e ->
                                    Log.w(TAG, "Error getting updated roll count", e)
                                    callback(-1)
                                }
                        }
                        .addOnFailureListener { e ->
                            Log.w(TAG, "Error incrementing roll count", e)
                            callback(-1)
                        }
                } else {
                    documentRef.set(mapOf("roll_count" to 1))
                        .addOnSuccessListener {
                            Log.d(TAG, "Roll count initialized to 1.")
                            callback(1)
                        }
                        .addOnFailureListener { e ->
                            Log.w(TAG, "Error initializing roll count", e)
                            callback(-1)
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error checking roll count document", e)
                callback(-1)
            }
    }

    fun readPics(callback: (MarsPhoto?, PicsumPhoto?) -> Unit) {
        firebase.collection(COLLECTION).document(APP_PICTURES_DOCUMENT)
            .get()
            .addOnSuccessListener { result ->
                val images = result.data as Map<String, String>
                val marsPhoto = images["mars"]?.let { Json.decodeFromString<MarsPhoto>(it) }
                val picsumPhoto = images["picsum"]?.let { Json.decodeFromString<PicsumPhoto>(it) }
                callback(marsPhoto, picsumPhoto)
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }
    }

    private fun save(document: String, map: HashMap<*, *>){
        firebase.collection(COLLECTION).document(document).set(map)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "DocumentSnapshot added with ID: $documentReference")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
            }
    }
}