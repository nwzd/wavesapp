package com.olapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.olapp.data.local.entity.MatchEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MatchDao {
    @Query("SELECT * FROM match ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<MatchEntity>>

    @Query("SELECT * FROM match WHERE id = :id")
    suspend fun getById(id: String): MatchEntity?

    @Query("SELECT * FROM match WHERE otherBleToken = :bleToken")
    suspend fun getByBleToken(bleToken: String): MatchEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(match: MatchEntity)

    @Query("DELETE FROM match WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT COUNT(*) FROM match")
    suspend fun getCount(): Int

    @Query("SELECT otherBleToken FROM match")
    suspend fun getAllOtherTokens(): List<String>

    @Query("UPDATE `match` SET latitude = :lat, longitude = :lon WHERE otherBleToken = :bleToken AND latitude IS NULL")
    suspend fun updateLocation(bleToken: String, lat: Double, lon: Double)

    @Query("DELETE FROM match")
    suspend fun deleteAll()
}