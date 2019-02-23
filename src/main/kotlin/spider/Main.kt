package spider

import api.downloadTopic
import api.spiderTopic
import download.Download
import download.DownloadSerializer
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.log4j.LogManager
import java.time.Duration
import java.util.*
import java.util.concurrent.Executors

fun main() = runBlocking {
    Main("localhost:9092").process()
}

class Main(brokers: String) {
    private val logger = LogManager.getLogger(javaClass)
    private val consumer = createConsumer(brokers)
    private val spiderProducer = createSpiderProducer(brokers)
    private val downloadProducer = createDownloadProducer(brokers)
    private val es = Executors.newSingleThreadExecutor()

    fun process() = runBlocking {
        consumer.subscribe(listOf(spiderTopic))
        while (true) {
            val records = poll(consumer, Duration.ofSeconds(5))
            val count = records.count()
            if (count == 0) {
                continue
            }

            logger.info("${threadName()} Received $count spiders")
            records.iterator().forEach {
                launch { spider(it.value()) }
            }
        }

    }

    @ObsoleteCoroutinesApi
    private fun poll(consumer: Consumer<String, Spider>, duration: Duration) = runBlocking(es.asCoroutineDispatcher()) {
        logger.info("${threadName()} KAFKA consumer.poll")
        consumer.poll(duration)
    }

    private fun threadName() = "[${Thread.currentThread().name}]"

    private fun spider(spider: Spider) {
        logger.info("${threadName()} spidering: $spider")
        try {
            logger.info("${threadName()} >> fetching page ${spider.pageNumber} for ${spider.subreddit}")
            val page = fetchPage(spider)
            produceSpider(spider, page.after)
            produceDownloads(page.children.map { child -> child.data })
        } catch (e: Exception) {
            logger.error("${threadName()} Issues fetching spider $spider")
            logger.error(e)
        }
    }

    private fun fetchPage(spider: Spider): SubredditData {
        logger.info("${threadName()} << FETCHED page ${spider.pageNumber} for ${spider.subreddit}")
        return loadSubreddit(spider.subreddit, spider.pageNumber, spider.paginationToken).data
    }

    private fun sanitizeUrl(url: String): String? {
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
        if (spider.pageNumber.toInt() >= spider.maxPages.toInt()) return
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
        props["group.id"] = "spiderer"

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