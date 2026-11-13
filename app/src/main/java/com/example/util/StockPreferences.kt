package com.example.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.stockUiDataStore: DataStore<Preferences> by preferencesDataStore(name = "stock_ui_prefs")

class StockPreferences(private val context: Context?) {

    private val fallbackGridState = MutableStateFlow(false)

    val isStockGridView: Flow<Boolean> = if (context != null) {
        context.applicationContext.stockUiDataStore.data
            .catch { exception ->
                if (exception is IOException) emit(emptyPreferences()) else throw exception
            }
            .map { prefs -> prefs[KEY_STOCK_GRID_VIEW] ?: false }
    } else {
        fallbackGridState
    }

    suspend fun setStockGridView(isGrid: Boolean) {
        if (context != null) {
            context.applicationContext.stockUiDataStore.edit { prefs ->
                prefs[KEY_STOCK_GRID_VIEW] = isGrid
            }
        } else {
            fallbackGridState.value = isGrid
        }
    }

    private companion object {
        val KEY_STOCK_GRID_VIEW = booleanPreferencesKey("stock_is_grid_view")
    }
}
