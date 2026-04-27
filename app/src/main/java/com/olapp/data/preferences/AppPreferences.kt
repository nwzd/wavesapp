package com.olapp.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ola_prefs")

@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    companion object {
        private val KEY_AGE_VERIFIED = booleanPreferencesKey("age_verified")
        private val KEY_AGE_VERIFICATION_DATE = longPreferencesKey("age_verification_date")
        private val KEY_SETUP_COMPLETE = booleanPreferencesKey("setup_complete")
        private val KEY_TERMS_ACCEPTED = booleanPreferencesKey("terms_accepted")
        private val KEY_ACCOUNT_RESTRICTED = booleanPreferencesKey("account_restricted")
        private val KEY_BLOCKED_BY_TOKENS = stringSetPreferencesKey("blocked_by_tokens")
        private val KEY_TUTORIAL_SEEN = booleanPreferencesKey("tutorial_seen")
    }

    val isAgeVerified: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_AGE_VERIFIED] ?: false
    }

    val isSetupComplete: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_SETUP_COMPLETE] ?: false
    }

    val isTermsAccepted: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_TERMS_ACCEPTED] ?: false
    }

    suspend fun setTermsAccepted(accepted: Boolean) {
        dataStore.edit { prefs -> prefs[KEY_TERMS_ACCEPTED] = accepted }
    }

    suspend fun setAgeVerified(verified: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_AGE_VERIFIED] = verified
            if (verified) {
                prefs[KEY_AGE_VERIFICATION_DATE] = System.currentTimeMillis()
            }
        }
    }

    suspend fun setSetupComplete(complete: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_SETUP_COMPLETE] = complete
        }
    }

    val isAccountRestricted: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_ACCOUNT_RESTRICTED] ?: false
    }

    suspend fun setAccountRestricted(restricted: Boolean) {
        dataStore.edit { prefs -> prefs[KEY_ACCOUNT_RESTRICTED] = restricted }
    }

    val isTutorialSeen: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_TUTORIAL_SEEN] ?: false
    }

    suspend fun setTutorialSeen() {
        dataStore.edit { prefs -> prefs[KEY_TUTORIAL_SEEN] = true }
    }

    // Records a block received from a unique token; returns total unique blocker count.
    suspend fun addBlockReceivedFrom(fromToken: String): Int {
        val after = dataStore.edit { prefs ->
            val current = prefs[KEY_BLOCKED_BY_TOKENS] ?: emptySet()
            prefs[KEY_BLOCKED_BY_TOKENS] = current + fromToken
        }
        return after[KEY_BLOCKED_BY_TOKENS]?.size ?: 0
    }

    suspend fun getBlockScore(): Int =
        dataStore.data.first()[KEY_BLOCKED_BY_TOKENS]?.size ?: 0
}