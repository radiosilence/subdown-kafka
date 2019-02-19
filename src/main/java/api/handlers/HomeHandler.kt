package api.handlers

import api.Bort
import api.BortSerializer
import api.bortTopic
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import ratpack.handling.Context
import ratpack.handling.Handler
import java.util.*
import org.apache.log4j.LogManager
import ratpack.jackson.Jackson.json

class HomeHandler(brokers: String) : Handler {
    private val logger = LogManager.getLogger(javaClass)
    private val producer = createProducer(brokers)
    private fun createProducer(brokers: String): Producer<String, Bort> {
        val props = Properties()
        props["bootstrap.servers"] = brokers
        props["key.serializer"] = StringSerializer::class.java
        props["value.serializer"] = BortSerializer::class.java
        return KafkaProducer<String, Bort>(props)
    }
    override fun handle(ctx: Context) {
        val bort = Bort("bortimus prime", 1984, Date())
        logger.info("Generated a bort: $bort")
        producer.send(ProducerRecord(bortTopic, bort))

        ctx.render(json(bort))
    }
}