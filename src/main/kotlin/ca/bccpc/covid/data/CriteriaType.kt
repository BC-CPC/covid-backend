package ca.bccpc.covid.data

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import java.lang.reflect.Type

enum class CriteriaType (
        val display: Boolean = true,
        private val columnName: String? = null,
        private val jsonName: String? = null
) {
    NAME(false),
    PUBLISHER,
    DATE(false),
    LINK(false),
    AUDIENCE,
    RESOURCE_TYPE(true, "Resource Type", "resourceType"),
    ORIGIN(true, "Location"),
    TOPIC;

    val column
        get() = columnName ?: name.toLowerCase().capitalize()
    val jsonKey
        get() = jsonName ?: name.toLowerCase()
}

class CriteriaAdapter: JsonAdapter<CriteriaType>(), JsonSerializer<CriteriaType> {
    override fun fromJson(reader: JsonReader): CriteriaType? {
        val key = reader.nextString()

        return CriteriaType.values().firstOrNull {
            it.jsonKey.equals(key, true)
        }
    }

    override fun toJson(writer: JsonWriter, value: CriteriaType?) {
        writer.value(value?.jsonKey)
    }

    override fun serialize(src: CriteriaType?, srcType: Type?, context: JsonSerializationContext?): JsonElement? {
        return JsonPrimitive(src?.jsonKey)
    }
}
