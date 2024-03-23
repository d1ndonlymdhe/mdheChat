package com.example.mdhechat.helpers

import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

suspend fun getTokenFromStore(store: DataStore<Preferences>): String? {
    val tokenKey = stringPreferencesKey("token")
    return store.data.firstOrNull()?.get(tokenKey)
}

suspend fun getUsernameFromStore(store: DataStore<Preferences>): String? {
    val usernameKey = stringPreferencesKey("username")
    return store.data.firstOrNull()?.get(usernameKey)
}