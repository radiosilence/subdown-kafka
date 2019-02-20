package api.handlers

import api.responses.StatusResponse
import ratpack.handling.Context
import ratpack.handling.Handler
import ratpack.jackson.Jackson.json

class StatusHandler : Handler {
    override fun handle(ctx: Context) {
        ctx.render(json(StatusResponse("ok")))
    }
}