package com.olapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocked_user")
data class BlockedUserEntity(
    @PrimaryKey val bleToken: String,
    val displayName: String,
    val blockedAt: Long = System.currentTimeMillis()
)
