package com.olapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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

@Database(
    entities = [
        UserProfileEntity::class,
        ReceivedOlaEntity::class,
        SentOlaEntity::class,
        MatchEntity::class,
        BlockedUserEntity::class
    ],
    version = 8,
    exportSchema = true
)
abstract class OlaDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun receivedOlaDao(): ReceivedOlaDao
    abstract fun sentOlaDao(): SentOlaDao
    abstract fun matchDao(): MatchDao
    abstract fun blockedUserDao(): BlockedUserDao

    companion object {
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE user_profile ADD COLUMN description TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE received_ola ADD COLUMN senderContactInfo TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE sent_ola ADD COLUMN receiverDisplayName TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE sent_ola ADD COLUMN receiverPhotoUrl TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE match ADD COLUMN latitude REAL")
                db.execSQL("ALTER TABLE match ADD COLUMN longitude REAL")
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS blocked_user (" +
                    "bleToken TEXT NOT NULL PRIMARY KEY, " +
                    "displayName TEXT NOT NULL, " +
                    "blockedAt INTEGER NOT NULL DEFAULT 0)"
                )
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE user_profile ADD COLUMN photoIsSelfie INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}
