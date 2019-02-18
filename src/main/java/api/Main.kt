package api

import api.handlers.HomeHandler
import api.handlers.StatusHandler
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.ser.std.StringSerializer
import com.fasterxml.jackson.databind.util.StdDateFormat
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import ratpack.server.RatpackServer
import java.util.*


object Main {
    @Throws(Exception::class)
    @JvmStatic
    fun main(args: Array<String>) {
        RatpackServer.start {
            it.handlers { chain ->
                chain
                    .get(HomeHandler())
                    .get("status", StatusHandler())
            }
        }
    }
    private fun createProducer(brokers: String): Producer<String, String> {
        val props = Properties()
        props["bootstrap.servers"] = brokers
        props["key.serializer"] = StringSerializer::class.java.canonicalName
        props["value.serializer"] = StringSerializer::class.java.canonicalName
        return KafkaProducer<String, String>(props)
    }
}