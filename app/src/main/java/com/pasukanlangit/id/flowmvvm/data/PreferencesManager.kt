package com.pasukanlangit.id.flowmvvm.data

import android.content.Context
import android.util.Log
import androidx.datastore.dataStore
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

enum class SortOrder { BY_NAME, BY_DATE }

data class FilterPreferences(val sortOrder: SortOrder, val hideCompleted: Boolean)

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext
    private val context: Context
) {
    private val Context.dataStore by preferencesDataStore(
        "user_preferences"
    )

    val preferencesFlow = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            }else{
                throw exception
            }
        }
        .map { preferences ->
            val sortOrder = SortOrder.valueOf(
                preferences[PreferencesKeys.SORT_ORDER] ?: SortOrder.BY_DATE.name
            )
            val hideCompleted = preferences[PreferencesKeys.HIDE_COMPLETED] ?: false
            FilterPreferences(sortOrder, hideCompleted)
        }

    suspend fun updateSortOrder(sortOrder: SortOrder){
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SORT_ORDER] = sortOrder.name
        }
    }

    suspend fun updateHideCompleted(isHideComplete: Boolean){
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HIDE_COMPLETED] = isHideComplete
        }
    }

    private object PreferencesKeys {
        val SORT_ORDER = stringPreferencesKey("sort_order")
        val HIDE_COMPLETED = booleanPreferencesKey("hide_completed")
    }
}