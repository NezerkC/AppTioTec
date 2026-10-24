package cl.duoc.tiotec.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "tiotec_preferences")

/**
 * Gestor de persistencia ligera clave-valor con Jetpack Preferences DataStore.
 * Almacena información de sesión y preferencias del perfil ficticio (Guía 12).
 */
class PreferenciasDataStore(private val context: Context) {

    companion object {
        val KEY_ALIAS_USUARIO = stringPreferencesKey("alias_usuario")
        val KEY_NIVEL_EXPERIENCIA = stringPreferencesKey("nivel_experiencia")
        val KEY_ULTIMO_DIAGNOSTICO_FECHA = stringPreferencesKey("ultimo_diagnostico_fecha")
        val KEY_ULTIMO_DIAGNOSTICO_TIMESTAMP = longPreferencesKey("ultimo_diagnostico_timestamp")
    }

    val aliasUsuarioFlow: Flow<String?> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_ALIAS_USUARIO]
        }

    val nivelExperienciaFlow: Flow<String?> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_NIVEL_EXPERIENCIA]
        }

    val ultimoDiagnosticoFechaFlow: Flow<String?> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_ULTIMO_DIAGNOSTICO_FECHA]
        }

    suspend fun guardarPerfil(alias: String, nivel: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_ALIAS_USUARIO] = alias
            preferences[KEY_NIVEL_EXPERIENCIA] = nivel
        }
    }

    suspend fun guardarUltimoDiagnostico(fecha: String, timestamp: Long = System.currentTimeMillis()) {
        context.dataStore.edit { preferences ->
            preferences[KEY_ULTIMO_DIAGNOSTICO_FECHA] = fecha
            preferences[KEY_ULTIMO_DIAGNOSTICO_TIMESTAMP] = timestamp
        }
    }

    suspend fun limpiarPreferencias() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
