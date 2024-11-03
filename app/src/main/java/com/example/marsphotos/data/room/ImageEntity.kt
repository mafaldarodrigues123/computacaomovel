package com.example.marsphotos.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "images")
data class ImageEntity(
    @PrimaryKey val id: Int = 1,
    val uri: String,
    val timestamp: Long = System.currentTimeMillis()
)