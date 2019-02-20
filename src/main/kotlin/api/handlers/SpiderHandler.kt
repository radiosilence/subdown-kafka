package api.handlers

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
        // TODO: There must be nicer ways of writing all of this
        val subredditString = ctx.allPathTokens["subreddits"]
        if (subredditString == null) {
            ctx.response.status(400).send("NO SUBREDDITS")
        }

        var maxPagesRaw = ctx.request.queryParams["maxPages"]
        if (maxPagesRaw.isNullOrBlank()) {
            maxPagesRaw = "1"
        }

        val subreddits = "$subredditString".split("+")
        subreddits.forEach {
            val spider = Spider(
                UUID.randomUUID(), it, 1, maxPagesRaw.toInt(), null)
                val result = producer . send (ProducerRecord(spiderTopic, spider))
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