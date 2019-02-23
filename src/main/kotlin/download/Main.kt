package download

import khttp.get
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
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
    private val es = Executors.newSingleThreadExecutor()

    fun process() = runBlocking {
        while (true) {
            val records = poll(consumer, Duration.ofSeconds(1))
            val count = records.count()
            if (count == 0) continue

            logger.info("${threadName()} received $count downloads")

            records.iterator().forEach {
                launch { download(it.value()) }
            }
        }

    }

    private fun threadName() = "[${Thread.currentThread().name}]"

    @ObsoleteCoroutinesApi
    private fun poll(consumer: Consumer<String, Download>, duration: Duration) =
        runBlocking(es.asCoroutineDispatcher()) {
            logger.info("${threadName()} KAFKA consumer.poll")
            consumer.poll(duration)
        }

    private fun download(post: Download) {
        logger.info("${threadName()}\"Downloading\" $post")
        if (post.url == null) return
        val r = get(
            url = post.url,
            headers = mapOf(
                "User-Agent" to "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.109 Safari/537.36",
                "Accept" to "application/json"
            )
        )

    }

    private fun createConsumer(brokers: String): Consumer<String, Download> {
        val props = Properties()
        props["bootstrap.servers"] = brokers
        props["key.deserializer"] = StringDeserializer::class.java
        props["value.deserializer"] = DownloadDeserializer::class.java
        props["group.id"] = "downloader"

        return KafkaConsumer<String, Download>(props)
    }
}