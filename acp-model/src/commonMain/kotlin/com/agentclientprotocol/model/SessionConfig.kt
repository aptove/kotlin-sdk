@file:Suppress("unused")
@file:OptIn(ExperimentalSerializationApi::class)

package com.agentclientprotocol.model

import com.agentclientprotocol.annotations.UnstableApi
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

/**
 * **UNSTABLE**
 *
 * This capability is not part of the spec yet, and may be removed or changed at any point.
 *
 * A single option for a session configuration select.
 */
@UnstableApi
@Serializable
public data class SessionConfigSelectOption(
    val value: SessionConfigValueId,
    val name: String,
    val description: String? = null,
    override val _meta: JsonElement? = null
) : AcpWithMeta

/**
 * **UNSTABLE**
 *
 * This capability is not part of the spec yet, and may be removed or changed at any point.
 *
 * A group of options for a session configuration select.
 */
@UnstableApi
@Serializable
public data class SessionConfigSelectGroup(
    val group: SessionConfigGroupId,
    val name: String,
    val options: List<SessionConfigSelectOption>,
    override val _meta: JsonElement? = null
) : AcpWithMeta

/**
 * **UNSTABLE**
 *
 * This capability is not part of the spec yet, and may be removed or changed at any point.
 *
 * Options for a session configuration select, either as a flat list or grouped.
 */
@UnstableApi
@Serializable(with = SessionConfigSelectOptionsSerializer::class)
public sealed class SessionConfigSelectOptions {
    /**
     * A flat list of options.
     */
    @Serializable
    public data class Flat(
        val options: List<SessionConfigSelectOption>
    ) : SessionConfigSelectOptions()

    /**
     * Options organized into groups.
     */
    @Serializable
    public data class Grouped(
        val groups: List<SessionConfigSelectGroup>
    ) : SessionConfigSelectOptions()
}

/**
 * **UNSTABLE**
 *
 * This capability is not part of the spec yet, and may be removed or changed at any point.
 *
 * Serializer for [SessionConfigSelectOptions]. The wire format is a bare JSON array —
 * either a flat list of [SessionConfigSelectOption] or a list of [SessionConfigSelectGroup].
 * We manually wrap the decoded elements into [Flat] or [Grouped].
 */
@OptIn(UnstableApi::class)
internal object SessionConfigSelectOptionsSerializer : KSerializer<SessionConfigSelectOptions> {
    // Use JsonElement's descriptor so the framework treats this as an opaque JSON value.
    override val descriptor: SerialDescriptor = JsonElement.serializer().descriptor

    override fun deserialize(decoder: Decoder): SessionConfigSelectOptions {
        val jsonDecoder = decoder as? JsonDecoder
            ?: throw SerializationException("SessionConfigSelectOptions requires a JSON decoder")
        val array = jsonDecoder.decodeJsonElement().jsonArray
        if (array.isEmpty()) return SessionConfigSelectOptions.Flat(emptyList())

        val firstElement = array[0].jsonObject
        return if ("group" in firstElement) {
            val groups = array.map {
                jsonDecoder.json.decodeFromJsonElement(SessionConfigSelectGroup.serializer(), it)
            }
            SessionConfigSelectOptions.Grouped(groups)
        } else {
            val options = array.map {
                jsonDecoder.json.decodeFromJsonElement(SessionConfigSelectOption.serializer(), it)
            }
            SessionConfigSelectOptions.Flat(options)
        }
    }

    override fun serialize(encoder: Encoder, value: SessionConfigSelectOptions) {
        val jsonEncoder = encoder as? JsonEncoder
            ?: throw SerializationException("SessionConfigSelectOptions requires a JSON encoder")
        val array = when (value) {
            is SessionConfigSelectOptions.Flat -> buildJsonArray {
                value.options.forEach { add(jsonEncoder.json.encodeToJsonElement(it)) }
            }
            is SessionConfigSelectOptions.Grouped -> buildJsonArray {
                value.groups.forEach { add(jsonEncoder.json.encodeToJsonElement(it)) }
            }
        }
        jsonEncoder.encodeJsonElement(array)
    }
}

/**
 * **UNSTABLE**
 *
 * This capability is not part of the spec yet, and may be removed or changed at any point.
 *
 * Configuration option types for sessions.
 */
@UnstableApi
@Serializable
@JsonClassDiscriminator("type")
public sealed class SessionConfigOption : AcpWithMeta {
    public abstract val id: SessionConfigId
    public abstract val name: String
    public abstract val description: String?

    /**
     * A select-type configuration option.
     */
    @Serializable
    @SerialName("select")
    public data class Select(
        override val id: SessionConfigId,
        override val name: String,
        override val description: String? = null,
        val currentValue: SessionConfigValueId,
        val options: SessionConfigSelectOptions,
        override val _meta: JsonElement? = null
    ) : SessionConfigOption()
}
