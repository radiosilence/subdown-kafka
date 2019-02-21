package spider

import api.downloadTopic
import api.spiderTopic
import download.Download
import download.DownloadSerializer
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
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
//            logger.info("!!!!!! POLLING !!!!!!!")
            val records = consumer.poll(Duration.ofSeconds(1))
            if (records.count() > 0) {
                logger.info("Received ${records.count()} spiders")
                GlobalScope.launch {
                    val results = records
                        .map { GlobalScope.async { processRecord(it) } }
                        .awaitAll()
                    logger.info("RESULTS: $results")
                }
            }
        }
    }

    private fun processRecord(record: ConsumerRecord<String, Spider>): Boolean {
        val spider = record.value()
        logger.info("SPIDER! $spider")
        try {
            logger.info(">> fetching page ${spider.pageNumber} for ${spider.subreddit}")
            val page = fetchPage(spider)
            produceSpider(spider, page.after)
            produceDownloads(page.children.map { child -> child.data })
        } catch (e: Exception) {
            logger.error("Issues fetching spider $spider")
            logger.error(e)
            return false
        }
        return true
    }

    private fun fetchPage(spider: Spider): SubredditData {
        logger.info("<< FETCHED page ${spider.pageNumber} for ${spider.subreddit}")
        return loadSubreddit(spider.subreddit, spider.pageNumber, spider.paginationToken).data
    }

    private fun sanitizeUrl(url: String): String? {
        // TODO: Can I use when here?
        return when {
            url.startsWith("https://i.redd.it") -> url
            url.startsWith("https://i.imgur.com") -> url
            else -> null
        }
    }

    private fun produceDownloads(links: List<Link>) {
        val downloads = links
            .map { Download(UUID.randomUUID(), it.id, sanitizeUrl(it.url), Date(it.created_utc.toLong())) }
            .filter { it.url !== null }


        downloads.forEach {
            logger.info("sending download $it")
            downloadProducer.send(ProducerRecord(downloadTopic, it))
        }
    }

    private fun produceSpider(spider: Spider, paginationToken: String?) {
        if (spider.pageNumber.toInt() >= spider.maxPages.toInt()) {
            return
        }
        val nextSpider =
            Spider(UUID.randomUUID(), spider.subreddit, spider.pageNumber.toInt() + 1, spider.maxPages, paginationToken)
        logger.info("making new spider $nextSpider")
        spiderProducer.send(ProducerRecord(spiderTopic, nextSpider))
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