package com.olapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    val uid: String,
    val displayName: String,
    val contactInfo: String,
    val photoUrl: String,
    val bleToken: String,
    val discoveryEnabled: Boolean,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
