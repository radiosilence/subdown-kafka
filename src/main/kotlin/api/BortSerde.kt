package api

import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serializer

class BortSerializer: Serializer<Bort> {
    override fun serialize(topic: String, data: Bort?): ByteArray? {
        if (data == null) return null
        return jsonMapper.writeValueAsBytes(data)
    }
    override fun close() {}
    override fun configure(configs: MutableMap<String, *>?, isKey: Boolean) {}
}

class BortDeserializer: Deserializer<Bort> {
    override fun deserialize(topic: String, data: ByteArray?): Bort? {
        if (data == null) return null
        return jsonMapper.readValue(data, Bort::class.java)
    }
    override fun close() {}
    override fun configure(configs: MutableMap<String, *>?, isKey: Boolean) {}
}