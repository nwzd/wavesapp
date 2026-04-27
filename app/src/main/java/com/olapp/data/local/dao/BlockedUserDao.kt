package com.olapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.olapp.data.local.entity.BlockedUserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockedUserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: BlockedUserEntity)

    @Query("SELECT * FROM blocked_user ORDER BY blockedAt DESC")
    fun observeAll(): Flow<List<BlockedUserEntity>>

    @Query("SELECT bleToken FROM blocked_user")
    suspend fun getAllTokens(): List<String>

    @Query("SELECT COUNT(*) FROM blocked_user WHERE bleToken = :token")
    suspend fun exists(token: String): Int

    @Query("DELETE FROM blocked_user WHERE bleToken = :token")
    suspend fun deleteByToken(token: String)

    @Query("DELETE FROM blocked_user")
    suspend fun deleteAll()
}
