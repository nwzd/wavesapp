package com.olapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "received_ola")
data class ReceivedOlaEntity(
    @PrimaryKey
    val id: String,
    val senderBleToken: String,
    val senderDisplayName: String,
    val senderPhotoUrl: String,
    val senderContactInfo: String = "",
    val senderDescription: String = "",
    val latitude: Double?,
    val longitude: Double?,
    val timestamp: Long = System.currentTimeMillis()
)