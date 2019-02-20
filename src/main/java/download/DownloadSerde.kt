package download

import api.jsonMapper
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serializer


class DownloadSerializer : Serializer<Download> {
    override fun serialize(topic: String, data: Download?): ByteArray? {
        if (data == null) return null
        return jsonMapper.writeValueAsBytes(data)
    }

    override fun close() {}
    override fun configure(configs: MutableMap<String, *>?, isKey: Boolean) {}
}

class DownloadDeserializer : Deserializer<Download> {
    override fun deserialize(topic: String, data: ByteArray?): Download? {
        if (data == null) return null
        return jsonMapper.readValue(data, Download::class.java)
    }

    override fun close() {}
    override fun configure(configs: MutableMap<String, *>?, isKey: Boolean) {}
}