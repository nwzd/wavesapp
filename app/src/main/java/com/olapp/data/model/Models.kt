package com.olapp.data.model

data class UserProfile(
    val uid: String,
    val displayName: String,
    val contactInfo: String,
    val photoUrl: String,
    val bleToken: String,
    val discoveryEnabled: Boolean,
    val description: String = ""
)

data class ReceivedOla(
    val id: String,
    val senderBleToken: String,
    val senderDisplayName: String,
    val senderPhotoUrl: String,
    val senderContactInfo: String = "",
    val latitude: Double?,
    val longitude: Double?,
    val timestamp: Long
)

data class SentOla(
    val id: String,
    val receiverBleToken: String,
    val receiverDisplayName: String = "",
    val receiverPhotoUrl: String = "",
    val latitude: Double?,
    val longitude: Double?,
    val timestamp: Long
)

data class Match(
    val id: String,
    val otherBleToken: String,
    val otherDisplayName: String,
    val otherPhotoUrl: String,
    val otherContactInfo: String,
    val createdAt: Long,
    val latitude: Double? = null,
    val longitude: Double? = null
)
