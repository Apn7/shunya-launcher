package dev.apn7.shunya.core.data

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.Serializer
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStoreFile
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

/**
 * The one JSON configuration for everything Shunya persists (DataStore files and backups).
 * Forgiving on read: unknown keys are ignored, unknown enum names and nulls fall back to the
 * property default, so older and newer app versions can read each other's files.
 */
val ShunyaJson: Json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
    coerceInputValues = true
    isLenient = true
}

/**
 * DataStore [Serializer] storing [T] as UTF-8 JSON. An unreadable file is reported as a
 * [CorruptionException], which [jsonDataStore]'s handler turns into [defaultValue].
 */
class JsonSerializer<T>(
    private val serializer: KSerializer<T>,
    override val defaultValue: T,
    private val json: Json = ShunyaJson,
) : Serializer<T> {

    override suspend fun readFrom(input: InputStream): T {
        val text = input.readBytes().decodeToString()
        if (text.isBlank()) return defaultValue
        return try {
            json.decodeFromString(serializer, text)
        } catch (e: IllegalArgumentException) {
            // kotlinx.serialization's SerializationException is an IllegalArgumentException.
            throw CorruptionException("Unreadable ${serializer.descriptor.serialName}", e)
        }
    }

    override suspend fun writeTo(t: T, output: OutputStream) {
        output.write(json.encodeToString(serializer, t).encodeToByteArray())
    }
}

/**
 * Creates a typed DataStore for `files/datastore/[fileName]`. A corrupt file is replaced by
 * [defaultValue] instead of crashing.
 *
 * DataStore allows exactly one instance per file per process: create stores only inside
 * `AppContainer` (as `by lazy` singletons) and never twice for the same [fileName].
 */
fun <T> Context.jsonDataStore(fileName: String, serializer: KSerializer<T>, defaultValue: T): DataStore<T> {
    val appContext = applicationContext
    return DataStoreFactory.create(
        serializer = JsonSerializer(serializer, defaultValue),
        corruptionHandler = ReplaceFileCorruptionHandler { defaultValue },
        produceFile = { appContext.dataStoreFile(fileName) },
    )
}
