package spider

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import khttp.get

fun loadSubreddit(subreddit: String, page: Number, paginationToken: String?): SubredditResponse {
    val r = get(
        url = "https://reddit.com/r/$subreddit.json",
        params = mapOf("page" to "$page", "after" to "$paginationToken"),
        headers = mapOf(
            "User-Agent" to "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.109 Safari/537.36",
            "Accept" to "application/json"
        )
    )
    val mapper = jacksonObjectMapper()
    return mapper.readValue(r.text)
}
