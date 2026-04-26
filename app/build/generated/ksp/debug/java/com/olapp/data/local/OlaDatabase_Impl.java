package com.olapp.data.local;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import com.olapp.data.local.dao.MatchDao;
import com.olapp.data.local.dao.MatchDao_Impl;
import com.olapp.data.local.dao.ReceivedOlaDao;
import com.olapp.data.local.dao.ReceivedOlaDao_Impl;
import com.olapp.data.local.dao.SentOlaDao;
import com.olapp.data.local.dao.SentOlaDao_Impl;
import com.olapp.data.local.dao.UserProfileDao;
import com.olapp.data.local.dao.UserProfileDao_Impl;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class OlaDatabase_Impl extends OlaDatabase {
  private volatile UserProfileDao _userProfileDao;

  private volatile ReceivedOlaDao _receivedOlaDao;

  private volatile SentOlaDao _sentOlaDao;

  private volatile MatchDao _matchDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(6) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `user_profile` (`uid` TEXT NOT NULL, `displayName` TEXT NOT NULL, `contactInfo` TEXT NOT NULL, `photoUrl` TEXT NOT NULL, `bleToken` TEXT NOT NULL, `discoveryEnabled` INTEGER NOT NULL, `description` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`uid`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `received_ola` (`id` TEXT NOT NULL, `senderBleToken` TEXT NOT NULL, `senderDisplayName` TEXT NOT NULL, `senderPhotoUrl` TEXT NOT NULL, `senderContactInfo` TEXT NOT NULL, `latitude` REAL, `longitude` REAL, `timestamp` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `sent_ola` (`id` TEXT NOT NULL, `receiverBleToken` TEXT NOT NULL, `receiverDisplayName` TEXT NOT NULL, `receiverPhotoUrl` TEXT NOT NULL, `latitude` REAL, `longitude` REAL, `timestamp` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `match` (`id` TEXT NOT NULL, `otherBleToken` TEXT NOT NULL, `otherDisplayName` TEXT NOT NULL, `otherPhotoUrl` TEXT NOT NULL, `otherContactInfo` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `latitude` REAL, `longitude` REAL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '81c8c71560c1ea173866682feac08bad')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `user_profile`");
        db.execSQL("DROP TABLE IF EXISTS `received_ola`");
        db.execSQL("DROP TABLE IF EXISTS `sent_ola`");
        db.execSQL("DROP TABLE IF EXISTS `match`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsUserProfile = new HashMap<String, TableInfo.Column>(8);
        _columnsUserProfile.put("uid", new TableInfo.Column("uid", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserProfile.put("displayName", new TableInfo.Column("displayName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserProfile.put("contactInfo", new TableInfo.Column("contactInfo", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserProfile.put("photoUrl", new TableInfo.Column("photoUrl", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserProfile.put("bleToken", new TableInfo.Column("bleToken", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserProfile.put("discoveryEnabled", new TableInfo.Column("discoveryEnabled", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserProfile.put("description", new TableInfo.Column("description", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserProfile.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysUserProfile = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesUserProfile = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoUserProfile = new TableInfo("user_profile", _columnsUserProfile, _foreignKeysUserProfile, _indicesUserProfile);
        final TableInfo _existingUserProfile = TableInfo.read(db, "user_profile");
        if (!_infoUserProfile.equals(_existingUserProfile)) {
          return new RoomOpenHelper.ValidationResult(false, "user_profile(com.olapp.data.local.entity.UserProfileEntity).\n"
                  + " Expected:\n" + _infoUserProfile + "\n"
                  + " Found:\n" + _existingUserProfile);
        }
        final HashMap<String, TableInfo.Column> _columnsReceivedOla = new HashMap<String, TableInfo.Column>(8);
        _columnsReceivedOla.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReceivedOla.put("senderBleToken", new TableInfo.Column("senderBleToken", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReceivedOla.put("senderDisplayName", new TableInfo.Column("senderDisplayName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReceivedOla.put("senderPhotoUrl", new TableInfo.Column("senderPhotoUrl", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReceivedOla.put("senderContactInfo", new TableInfo.Column("senderContactInfo", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReceivedOla.put("latitude", new TableInfo.Column("latitude", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReceivedOla.put("longitude", new TableInfo.Column("longitude", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReceivedOla.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysReceivedOla = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesReceivedOla = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoReceivedOla = new TableInfo("received_ola", _columnsReceivedOla, _foreignKeysReceivedOla, _indicesReceivedOla);
        final TableInfo _existingReceivedOla = TableInfo.read(db, "received_ola");
        if (!_infoReceivedOla.equals(_existingReceivedOla)) {
          return new RoomOpenHelper.ValidationResult(false, "received_ola(com.olapp.data.local.entity.ReceivedOlaEntity).\n"
                  + " Expected:\n" + _infoReceivedOla + "\n"
                  + " Found:\n" + _existingReceivedOla);
        }
        final HashMap<String, TableInfo.Column> _columnsSentOla = new HashMap<String, TableInfo.Column>(7);
        _columnsSentOla.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSentOla.put("receiverBleToken", new TableInfo.Column("receiverBleToken", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSentOla.put("receiverDisplayName", new TableInfo.Column("receiverDisplayName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSentOla.put("receiverPhotoUrl", new TableInfo.Column("receiverPhotoUrl", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSentOla.put("latitude", new TableInfo.Column("latitude", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSentOla.put("longitude", new TableInfo.Column("longitude", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSentOla.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSentOla = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesSentOla = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoSentOla = new TableInfo("sent_ola", _columnsSentOla, _foreignKeysSentOla, _indicesSentOla);
        final TableInfo _existingSentOla = TableInfo.read(db, "sent_ola");
        if (!_infoSentOla.equals(_existingSentOla)) {
          return new RoomOpenHelper.ValidationResult(false, "sent_ola(com.olapp.data.local.entity.SentOlaEntity).\n"
                  + " Expected:\n" + _infoSentOla + "\n"
                  + " Found:\n" + _existingSentOla);
        }
        final HashMap<String, TableInfo.Column> _columnsMatch = new HashMap<String, TableInfo.Column>(8);
        _columnsMatch.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMatch.put("otherBleToken", new TableInfo.Column("otherBleToken", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMatch.put("otherDisplayName", new TableInfo.Column("otherDisplayName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMatch.put("otherPhotoUrl", new TableInfo.Column("otherPhotoUrl", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMatch.put("otherContactInfo", new TableInfo.Column("otherContactInfo", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMatch.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMatch.put("latitude", new TableInfo.Column("latitude", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMatch.put("longitude", new TableInfo.Column("longitude", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysMatch = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesMatch = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoMatch = new TableInfo("match", _columnsMatch, _foreignKeysMatch, _indicesMatch);
        final TableInfo _existingMatch = TableInfo.read(db, "match");
        if (!_infoMatch.equals(_existingMatch)) {
          return new RoomOpenHelper.ValidationResult(false, "match(com.olapp.data.local.entity.MatchEntity).\n"
                  + " Expected:\n" + _infoMatch + "\n"
                  + " Found:\n" + _existingMatch);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "81c8c71560c1ea173866682feac08bad", "ebb2cd00172a8af9f441aec10dbf48eb");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "user_profile","received_ola","sent_ola","match");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `user_profile`");
      _db.execSQL("DELETE FROM `received_ola`");
      _db.execSQL("DELETE FROM `sent_ola`");
      _db.execSQL("DELETE FROM `match`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(UserProfileDao.class, UserProfileDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(ReceivedOlaDao.class, ReceivedOlaDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(SentOlaDao.class, SentOlaDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(MatchDao.class, MatchDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public UserProfileDao userProfileDao() {
    if (_userProfileDao != null) {
      return _userProfileDao;
    } else {
      synchronized(this) {
        if(_userProfileDao == null) {
          _userProfileDao = new UserProfileDao_Impl(this);
        }
        return _userProfileDao;
      }
    }
  }

  @Override
  public ReceivedOlaDao receivedOlaDao() {
    if (_receivedOlaDao != null) {
      return _receivedOlaDao;
    } else {
      synchronized(this) {
        if(_receivedOlaDao == null) {
          _receivedOlaDao = new ReceivedOlaDao_Impl(this);
        }
        return _receivedOlaDao;
      }
    }
  }

  @Override
  public SentOlaDao sentOlaDao() {
    if (_sentOlaDao != null) {
      return _sentOlaDao;
    } else {
      synchronized(this) {
        if(_sentOlaDao == null) {
          _sentOlaDao = new SentOlaDao_Impl(this);
        }
        return _sentOlaDao;
      }
    }
  }

  @Override
  public MatchDao matchDao() {
    if (_matchDao != null) {
      return _matchDao;
    } else {
      synchronized(this) {
        if(_matchDao == null) {
          _matchDao = new MatchDao_Impl(this);
        }
        return _matchDao;
      }
    }
  }
}
