package com.olapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "match")
data class MatchEntity(
    @PrimaryKey
    val id: String,
    val otherBleToken: String,
    val otherDisplayName: String,
    val otherPhotoUrl: String,
    val otherContactInfo: String,
    val otherDescription: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val latitude: Double? = null,
    val longitude: Double? = null
)