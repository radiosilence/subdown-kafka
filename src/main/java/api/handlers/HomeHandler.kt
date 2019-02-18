package api.handlers

import ratpack.handling.Context
import ratpack.handling.Handler

class HomeHandler : Handler {
    override fun handle(ctx: Context) {
        ctx.render("Arse!")
    }
}