package concurrency

import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.Executors

fun main() {
    Main().process()
}

class Main {
    private val es = Executors.newSingleThreadExecutor()

    fun process() {

        runBlocking {
            delay(2000)
            println("FIRST")
            while (true) {
                println("LOOP")// simulating kafka consumer.poll(... )
                launch {
                    println("INNER THING")
                }
                launch {
                    delay(500)
                    println("INNER THING")
                }
                val blockResult = execute()

                println("Block result $blockResult")
            }
        }
    }

    private fun execute() = runBlocking(es.asCoroutineDispatcher()) {
        Thread.sleep(1000) // simulating a slow command
        1
    }
}
