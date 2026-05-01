package com.olapp.data.repository

import android.annotation.SuppressLint
import com.olapp.data.local.dao.BlockedUserDao
import com.olapp.data.local.dao.MatchDao
import com.olapp.data.local.dao.ReceivedOlaDao
import com.olapp.data.local.dao.SentOlaDao
import com.olapp.data.local.dao.UserProfileDao
import com.olapp.data.local.entity.BlockedUserEntity
import com.olapp.data.local.entity.MatchEntity
import com.olapp.data.local.entity.ReceivedOlaEntity
import com.olapp.data.local.entity.SentOlaEntity
import com.olapp.data.local.entity.UserProfileEntity
import com.olapp.data.model.Match
import com.olapp.data.model.ReceivedOla
import com.olapp.data.model.SentOla
import com.olapp.data.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userProfileDao: UserProfileDao,
    private val receivedOlaDao: ReceivedOlaDao,
    private val sentOlaDao: SentOlaDao,
    private val matchDao: MatchDao,
    private val blockedUserDao: BlockedUserDao
) {
    private val matchMutex = Mutex()
    fun observeMyProfile(): Flow<UserProfile?> =
        userProfileDao.observeMyProfile().map { it?.toModel() }

    suspend fun getMyProfile(): UserProfile? =
        userProfileDao.getMyProfile()?.toModel()

    @SuppressLint("MissingPermission")
    suspend fun getMyBleToken(): String? =
        userProfileDao.getMyProfile()?.bleToken

    suspend fun saveProfile(
        displayName: String,
        contactInfo: String,
        photoUrl: String,
        discoveryEnabled: Boolean,
        description: String = "",
        photoIsSelfie: Boolean = false
    ): UserProfile {
        val existing = userProfileDao.getMyProfile()
        val bleToken = existing?.bleToken ?: generateBleToken()
        val uid = existing?.uid ?: UUID.randomUUID().toString()

        val entity = UserProfileEntity(
            uid = uid,
            displayName = displayName,
            contactInfo = contactInfo,
            photoUrl = photoUrl,
            bleToken = bleToken,
            discoveryEnabled = discoveryEnabled,
            description = description,
            photoIsSelfie = photoIsSelfie
        )
        userProfileDao.insert(entity)
        return entity.toModel()
    }

    suspend fun updateDiscovery(enabled: Boolean) {
        userProfileDao.updateDiscovery(enabled)
    }

    fun observeMatches(): Flow<List<Match>> =
        matchDao.observeAll().map { list -> list.map { it.toModel() } }

    suspend fun createMatch(
        otherBleToken: String,
        otherDisplayName: String,
        otherPhotoUrl: String,
        otherContactInfo: String,
        otherDescription: String = "",
        latitude: Double? = null,
        longitude: Double? = null
    ): Match? = matchMutex.withLock {
        val existing = matchDao.getByBleToken(otherBleToken)
        if (existing != null) return@withLock existing.toModel()

        val match = Match(
            id = UUID.randomUUID().toString(),
            otherBleToken = otherBleToken,
            otherDisplayName = otherDisplayName,
            otherPhotoUrl = otherPhotoUrl,
            otherContactInfo = otherContactInfo,
            otherDescription = otherDescription,
            createdAt = System.currentTimeMillis(),
            latitude = latitude,
            longitude = longitude
        )
        matchDao.insert(match.toEntity())
        match
    }

    fun observeReceivedOlas(): Flow<List<ReceivedOla>> =
        receivedOlaDao.observeAll().map { list -> list.map { it.toModel() } }

    fun observeReceivedOlaCount(): Flow<Int> = receivedOlaDao.observeCount()

    fun observeReceivedOlaCountSince(since: Long): Flow<Int> = receivedOlaDao.observeCountSince(since)

    suspend fun saveReceivedOla(
        senderBleToken: String,
        senderDisplayName: String,
        senderPhotoUrl: String,
        senderContactInfo: String = "",
        senderDescription: String = "",
        latitude: Double?,
        longitude: Double?
    ): ReceivedOla {
        val existing = receivedOlaDao.getLatestByBleToken(senderBleToken)
        if (existing != null && System.currentTimeMillis() - existing.timestamp < 60_000L) {
            return existing.toModel()
        }

        val ola = ReceivedOlaEntity(
            id = UUID.randomUUID().toString(),
            senderBleToken = senderBleToken,
            senderDisplayName = senderDisplayName,
            senderPhotoUrl = senderPhotoUrl,
            senderContactInfo = senderContactInfo,
            senderDescription = senderDescription,
            latitude = latitude,
            longitude = longitude,
            timestamp = System.currentTimeMillis()
        )
        receivedOlaDao.insert(ola)
        return ola.toModel()
    }

    suspend fun getLatestReceivedOlaFrom(bleToken: String): ReceivedOla? =
        receivedOlaDao.getLatestByBleToken(bleToken)?.toModel()

    suspend fun evictStaleReceivedOlas(cutoff: Long) {
        receivedOlaDao.deleteOlderThan(cutoff)
    }

    fun observeSentOlas(): Flow<List<SentOla>> =
        sentOlaDao.observeAll().map { list -> list.map { it.toModel() } }

    suspend fun saveSentOla(
        receiverBleToken: String,
        receiverDisplayName: String = "",
        receiverPhotoUrl: String = "",
        receiverDescription: String = "",
        latitude: Double?,
        longitude: Double?
    ): SentOla {
        val existing = sentOlaDao.getLatestByBleToken(receiverBleToken)
        if (existing != null && System.currentTimeMillis() - existing.timestamp < 60_000L) {
            return existing.toModel()
        }

        val ola = SentOlaEntity(
            id = UUID.randomUUID().toString(),
            receiverBleToken = receiverBleToken,
            receiverDisplayName = receiverDisplayName,
            receiverPhotoUrl = receiverPhotoUrl,
            receiverDescription = receiverDescription,
            latitude = latitude,
            longitude = longitude,
            timestamp = System.currentTimeMillis()
        )
        sentOlaDao.insert(ola)
        return ola.toModel()
    }

    suspend fun hasSentOlaTo(bleToken: String): Boolean =
        sentOlaDao.getLatestByBleToken(bleToken) != null

    suspend fun hasMatchWith(bleToken: String): Boolean =
        matchDao.getByBleToken(bleToken) != null

    suspend fun updateMatchLocation(otherBleToken: String, lat: Double, lon: Double) {
        matchDao.updateLocation(otherBleToken, lat, lon)
    }

    suspend fun getUnrespondedWaveCount(): Int = receivedOlaDao.getUnrespondedCount()

    suspend fun getMatchCount(): Int = matchDao.getCount()

    suspend fun getExcludedTokens(): Set<String> =
        matchDao.getAllOtherTokens().toHashSet<String>().also {
            it.addAll(sentOlaDao.getAllReceiverTokens())
        }

    suspend fun evictStaleSentOlas(cutoff: Long) {
        sentOlaDao.deleteOlderThan(cutoff)
    }

    suspend fun deleteReceivedOla(id: String) = receivedOlaDao.deleteById(id)

    suspend fun deleteSentOla(id: String) = sentOlaDao.deleteById(id)

    suspend fun deleteMatch(matchId: String, otherBleToken: String) {
        matchDao.deleteById(matchId)
        receivedOlaDao.deleteByBleToken(otherBleToken)
        sentOlaDao.deleteByBleToken(otherBleToken)
    }

    suspend fun blockUser(token: String, displayName: String) {
        blockedUserDao.insert(BlockedUserEntity(bleToken = token, displayName = displayName))
        matchDao.deleteByBleToken(token)
        receivedOlaDao.deleteByBleToken(token)
        sentOlaDao.deleteByBleToken(token)
    }

    suspend fun getBlockedTokens(): Set<String> = blockedUserDao.getAllTokens().toHashSet()

    suspend fun clearAll() {
        matchDao.deleteAll()
        receivedOlaDao.deleteAll()
        sentOlaDao.deleteAll()
    }

    private fun generateBleToken(): String {
        val bytes = ByteArray(8)
        java.security.SecureRandom().nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun UserProfileEntity.toModel() = UserProfile(
        uid = uid,
        displayName = displayName,
        contactInfo = contactInfo,
        photoUrl = photoUrl,
        bleToken = bleToken,
        discoveryEnabled = discoveryEnabled,
        description = description,
        photoIsSelfie = photoIsSelfie
    )

    private fun ReceivedOlaEntity.toModel() = ReceivedOla(
        id = id,
        senderBleToken = senderBleToken,
        senderDisplayName = senderDisplayName,
        senderPhotoUrl = senderPhotoUrl,
        senderContactInfo = senderContactInfo,
        senderDescription = senderDescription,
        latitude = latitude,
        longitude = longitude,
        timestamp = timestamp
    )

    private fun SentOlaEntity.toModel() = SentOla(
        id = id,
        receiverBleToken = receiverBleToken,
        receiverDisplayName = receiverDisplayName,
        receiverPhotoUrl = receiverPhotoUrl,
        receiverDescription = receiverDescription,
        latitude = latitude,
        longitude = longitude,
        timestamp = timestamp
    )

    private fun MatchEntity.toModel() = Match(
        id = id,
        otherBleToken = otherBleToken,
        otherDisplayName = otherDisplayName,
        otherPhotoUrl = otherPhotoUrl,
        otherContactInfo = otherContactInfo,
        otherDescription = otherDescription,
        createdAt = createdAt,
        latitude = latitude,
        longitude = longitude
    )

    private fun Match.toEntity() = MatchEntity(
        id = id,
        otherBleToken = otherBleToken,
        otherDisplayName = otherDisplayName,
        otherPhotoUrl = otherPhotoUrl,
        otherContactInfo = otherContactInfo,
        otherDescription = otherDescription,
        createdAt = createdAt,
        latitude = latitude,
        longitude = longitude
    )
}