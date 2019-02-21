package api.handlers

import api.responses.ErrorResponse
import api.spiderTopic
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.log4j.LogManager
import ratpack.handling.Context
import ratpack.handling.Handler
import ratpack.jackson.Jackson.json
import spider.Spider
import spider.SpiderSerializer
import java.util.*

class SpiderHandler(brokers: String) : Handler {
    private val logger = LogManager.getLogger(javaClass)
    private val producer = createProducer(brokers)
    override fun handle(ctx: Context) {
        // TODO: There must be a more idiomatic way of writing all of this
        val maxPages = ctx.request.queryParams.getOrDefault("maxPages", "1").toIntOrNull()

        if (maxPages === null) {
            ctx.response.status(400)
            return ctx.render(json(ErrorResponse("Bad maxPages")))
        }

        ctx.allPathTokens["subreddits"]?.split("+")?.forEach {
            val spider = Spider(UUID.randomUUID(), it, 1, maxPages, null)
            val result = producer.send(ProducerRecord(spiderTopic, spider))
            logger.info("Generated a $spider $result")
        }

        ctx.render("Sent")
    }

    private fun createProducer(brokers: String): Producer<String, Spider> {
        val props = Properties()
        props["bootstrap.servers"] = brokers
        props["key.serializer"] = StringSerializer::class.java
        props["value.serializer"] = SpiderSerializer::class.java
        return KafkaProducer<String, Spider>(props)
    }
}