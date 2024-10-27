package com.example.marsphotos.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class PicsumPhoto(
    val id: String,
    val author: String,
    val width: Int,
    val height: Int,
    val url: String,
    @SerialName(value = "download_url")
    val downloadUrl: String
    ){companion object{
        override fun toString(): String {
            return Json.encodeToString(this)
        }

        fun decodeFromString(str: String): PicsumPhoto{
            return Json.decodeFromString<PicsumPhoto>(str)
        }
}
}