package spider

import api.spiderTopic
import download.Download
import download.DownloadSerializer
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.log4j.LogManager
import java.time.Duration
import java.util.*

fun main(args: Array<String>) {
    SpiderProcessor("localhost:9092").process()
}

class SpiderProcessor(brokers: String) {
    private val logger = LogManager.getLogger(javaClass)
    private val consumer = createConsumer(brokers)
    private val spiderProducer = createSpiderProducer(brokers)
    private val downloadProducer = createDownloadProducer(brokers)

    fun process() {
        consumer.subscribe(listOf(spiderTopic))

        while (true) {
            val records = consumer.poll(Duration.ofSeconds(1))
            logger.info("Received ${records.count()} spiders")

            records.iterator().forEach {
                val spider = it.value()
                logger.info("SPIDER! $spider")
            }
        }
    }

    private fun fetchPage(spider: Spider) {
        // TODO: fetches page
        // 1. Fetch page
        // 2. if (pageNumber + 1) < maxPages, produce a spider for the next page
        // 3. for each image link on page, produce a download
    }

    private fun createConsumer(brokers: String): Consumer<String, Spider> {
        val props = Properties()

        props["bootstrap.servers"] = brokers
        props["group.id"] = "spider-processor"
        props["key.deserializer"] = StringDeserializer::class.java
        props["value.deserializer"] = SpiderDeserializer::class.java

        return KafkaConsumer<String, Spider>(props)
    }

    private fun createSpiderProducer(brokers: String): Producer<String, Spider> {
        val props = Properties()

        props["bootstrap.servers"] = brokers
        props["key.serializer"] = StringSerializer::class.java
        props["value.serializer"] = SpiderSerializer::class.java

        return KafkaProducer<String, Spider>(props)
    }

    private fun createDownloadProducer(brokers: String): Producer<String, Download> {
        val props = Properties()

        props["bootstrap.servers"] = brokers
        props["key.serializer"] = StringSerializer::class.java
        props["value.serializer"] = DownloadSerializer::class.java

        return KafkaProducer<String, Download>(props)
    }

}