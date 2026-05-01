package com.olapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sent_ola")
data class SentOlaEntity(
    @PrimaryKey
    val id: String,
    val receiverBleToken: String,
    val receiverDisplayName: String = "",
    val receiverPhotoUrl: String = "",
    val receiverDescription: String = "",
    val latitude: Double?,
    val longitude: Double?,
    val timestamp: Long = System.currentTimeMillis()
)