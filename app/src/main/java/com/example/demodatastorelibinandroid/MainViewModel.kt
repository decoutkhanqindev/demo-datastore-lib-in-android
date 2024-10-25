package com.example.demodatastorelibinandroid

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Create a DataStore instance using the preferencesDataStore delegate, with the Context as
// receiver.
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "Settings")

class MainViewModel(application: Application) : AndroidViewModel(application = application) {
  // DataStore does help with serialization (saving data to storage)
  // and deserialization (retrieving and reading data back
  
  // DataStore operations are thread-safe and atomic, meaning they won’t overlap in a way
  // that leads to inconsistent states. If multiple updates are called, they are queued
  // and executed sequentially to prevent conflicts.
  
  // To see files by DataStore saved:
  // View -> Tool Windows -> data -> data -> com. .... nameProject -> files -> datastore
  
  private val dataStore: DataStore<Preferences> = application.dataStore // or use dependency injection
  
  private var counter: Int = 0
  
  val uiStateFlow: StateFlow<DataStoreUiState?> =
    dataStore.data
      .onStart {
        println(">>>> STARTED collecting data")
      }
      .onCompletion {
        println(">>>> COMPLETED collecting data")
      }
      .catch { throwable: Throwable ->
        if (throwable is IOException) {
          // empty preferences/state when reading/writing data
          // (storage issues or file corruption)
          emit(emptyPreferences())
        } else {
          throw throwable
        }
      }
      .map { prefs: Preferences ->
        DataStoreUiState(
          counter = prefs[COUNTER_KEY] ?: 0,
          darkTheme = prefs[DARK_THEME] ?: false
        )
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
        initialValue = null
      )
  
  fun updateData() {
    viewModelScope.launch {
//      dataStore.updateData { currentPrefs: Preferences ->
//        currentPrefs.toMutablePreferences().apply {
//          // this.set(COUNTER_KEY, (this.get(COUNTER_KEY) ?: 0) + 1)
//          // this.set(DARK_THEME, this.get(DARK_THEME) ?: false)
//          this[COUNTER_KEY] = (this[COUNTER_KEY] ?: 0) + 1
//          this[DARK_THEME] = this[DARK_THEME] ?: false
//        }
//      }
      
      // or
      val curUpdateCounter: Int = counter++
      
      dataStore.edit { currentMutablePrefs: MutablePreferences ->
        println(">>>> STARTED UPDATE #$curUpdateCounter")
        
        delay(1000) // simulate long running operation.... (call api)
        
        currentMutablePrefs[COUNTER_KEY] = (currentMutablePrefs[COUNTER_KEY] ?: 0) + 1
        currentMutablePrefs[DARK_THEME] = currentMutablePrefs[DARK_THEME] ?: false
        
        println(">>>> FINISHED UPDATE #$curUpdateCounter")
      }
    }
  }
  
  fun clearData() {
    viewModelScope.launch {
      val curClearCounter: Int = counter++
      
      dataStore.edit { currentMutablePrefs: MutablePreferences ->
        println(">>>> STARTED CLEAR #$curClearCounter")
        
        delay(2000)
        
        currentMutablePrefs.clear()
        
        println(">>>> FINISHED CLEAR #$curClearCounter")
      }
    }
  }
  
  companion object {
    private val COUNTER_KEY: Preferences.Key<Int> = intPreferencesKey("counter_key")
    private val DARK_THEME: Preferences.Key<Boolean> = booleanPreferencesKey("dark_theme")
  }
}