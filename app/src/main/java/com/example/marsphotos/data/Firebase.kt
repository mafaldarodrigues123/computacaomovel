package com.example.marsphotos.data

import android.util.Log
import com.example.marsphotos.model.MarsPhoto
import com.example.marsphotos.model.PicsumPhoto
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

const val TAG = "Firebase"

class Firebase() {

    private val firebase = Firebase.firestore

    fun save(marsPhoto: MarsPhoto, picsumPhoto: PicsumPhoto) {
        val images = hashMapOf(
            "mars" to Json.encodeToString(marsPhoto),
            "picsum" to Json.encodeToString(picsumPhoto)
        )
        //firebase.collection("images").document().delete()
        firebase.collection("saved_images").add(images)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
            }
    }

    fun read(callback: (MarsPhoto?, PicsumPhoto?) -> Unit) {
        firebase.collection("saved_images")
            .get()
            .addOnSuccessListener { result ->
                val images = result.last().data as Map<String, String>
                val marsPhoto = images["mars"]?.let { Json.decodeFromString<MarsPhoto>(it) }
                val picsumPhoto = images["picsum"]?.let { Json.decodeFromString<PicsumPhoto>(it) }
                callback(marsPhoto, picsumPhoto)
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }
    }
}