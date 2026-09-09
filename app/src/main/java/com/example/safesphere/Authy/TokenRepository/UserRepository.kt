package com.example.safesphere.Authy.TokenRepository

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(
    name = "user_preferences"
)

class UserRepository(private val context: Context) {
    private val dataStore = context.dataStore

    companion object {
        private val ACCESS_TOKEN = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        private val USERNAME = stringPreferencesKey("username")
        private val PHONE_NUMBER = stringPreferencesKey("phone_number")
    }

    suspend fun saveUserData(
        accessToken: String,
        refreshToken: String,
        username: String,
        phoneNumber: String
    ) {
        dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN] = accessToken
            preferences[REFRESH_TOKEN] = refreshToken
            preferences[USERNAME] = username
            preferences[PHONE_NUMBER] = phoneNumber
        }
    }

    val accessToken: Flow<String?> =
        dataStore.data.map { it[ACCESS_TOKEN] }

    val refreshToken: Flow<String?> =
        dataStore.data.map { it[REFRESH_TOKEN] }

    val username: Flow<String?> =
        dataStore.data.map { it[USERNAME] }

    val phoneNumber: Flow<String?> =
        dataStore.data.map { it[PHONE_NUMBER] }
}