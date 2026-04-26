package com.olapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.olapp.data.local.entity.SentOlaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SentOlaDao {
    @Query("SELECT * FROM sent_ola ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<SentOlaEntity>>

    @Query("SELECT * FROM sent_ola WHERE receiverBleToken = :bleToken ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestByBleToken(bleToken: String): SentOlaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(ola: SentOlaEntity)

    @Query("DELETE FROM sent_ola WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM sent_ola WHERE receiverBleToken = :bleToken")
    suspend fun deleteByBleToken(bleToken: String)

    @Query("DELETE FROM sent_ola WHERE timestamp < :cutoff")
    suspend fun deleteOlderThan(cutoff: Long)

    @Query("SELECT receiverBleToken FROM sent_ola")
    suspend fun getAllReceiverTokens(): List<String>

    @Query("DELETE FROM sent_ola")
    suspend fun deleteAll()
}