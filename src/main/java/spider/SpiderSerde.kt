package spider


import api.jsonMapper
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serializer

class SpiderSerializer : Serializer<Spider> {
    override fun serialize(topic: String, data: Spider?): ByteArray? {
        if (data == null) return null
        return jsonMapper.writeValueAsBytes(data)
    }

    override fun close() {}
    override fun configure(configs: MutableMap<String, *>?, isKey: Boolean) {}
}

class BortDeserializer : Deserializer<Spider> {
    override fun deserialize(topic: String, data: ByteArray?): Spider? {
        if (data == null) return null
        return jsonMapper.readValue(data, Spider::class.java)
    }

    override fun close() {}
    override fun configure(configs: MutableMap<String, *>?, isKey: Boolean) {}
}