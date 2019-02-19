package api

import api.handlers.HomeHandler
import api.handlers.StatusHandler

import ratpack.server.RatpackServer


object Main {
    @Throws(Exception::class)
    @JvmStatic
    fun main(args: Array<String>) {
        RatpackServer.start {
            it.handlers { chain ->
                chain
                    .get(HomeHandler("localhost:9092"))
                    .get("status", StatusHandler())
            }
        }
    }
}