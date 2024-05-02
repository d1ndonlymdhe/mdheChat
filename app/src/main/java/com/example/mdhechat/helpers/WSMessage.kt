package com.example.mdhechat.helpers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json


@Serializable
data class WSEvent(val event: String)

//@Serializable(with = (WSDataSerializer::class))
@Serializable
data class WSData<T>(val data: T)

@Serializable
data class WSMessage<T>(
    val event: String, val data: T
)

class WSDataSerializer<T>(private val dataSerializer: KSerializer<T>) :
    KSerializer<WSData<T>> {
    override val descriptor: SerialDescriptor = dataSerializer.descriptor
    override fun serialize(encoder: Encoder, value: WSData<T>) {
        dataSerializer.serialize(encoder, value.data)
    }

    override fun deserialize(decoder: Decoder): WSData<T> {
        return WSData(dataSerializer.deserialize(decoder))
    }
}


fun getWSEvent(data: String): String {
    return JSON.decodeFromString<WSEvent>(data).event
}

@Serializable
data class NInfo(val id: String?, val k: String?, val v: String?)

inline fun <reified T> getWSData(data: String): T {
    return JSON.decodeFromString<WSData<T>>(data).data
}

