package api

import api.handlers.HomeHandler
import api.handlers.SpiderHandler
import api.handlers.StatusHandler

import ratpack.server.RatpackServer

fun main(args: Array<String>) {
    Main("localhost:9092").main(args)
}

class Main(brokers: String) {
    private val brokers = brokers
    @Throws(Exception::class)
    fun main(args: Array<String>) {
        RatpackServer.start {
            it.handlers { chain ->
                chain
                    .get(HomeHandler(brokers))
                    .get("status", StatusHandler())
                    .get("r/:subreddits", SpiderHandler(brokers))
            }
        }
    }
}