package com.olapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.olapp.data.local.entity.ReceivedOlaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReceivedOlaDao {
    @Query("SELECT * FROM received_ola ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<ReceivedOlaEntity>>

    @Query("SELECT * FROM received_ola WHERE senderBleToken = :bleToken ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestByBleToken(bleToken: String): ReceivedOlaEntity?

    @Query("SELECT COUNT(*) FROM received_ola")
    fun observeCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM received_ola WHERE timestamp > :since")
    fun observeCountSince(since: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM received_ola WHERE senderBleToken NOT IN (SELECT otherBleToken FROM match)")
    suspend fun getUnrespondedCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(ola: ReceivedOlaEntity)

    @Query("DELETE FROM received_ola WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM received_ola WHERE senderBleToken = :bleToken")
    suspend fun deleteByBleToken(bleToken: String)

    @Query("DELETE FROM received_ola WHERE timestamp < :cutoff")
    suspend fun deleteOlderThan(cutoff: Long)

    @Query("DELETE FROM received_ola")
    suspend fun deleteAll()
}