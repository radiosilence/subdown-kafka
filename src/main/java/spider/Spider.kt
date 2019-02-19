package spider

import java.util.*

data class Spider(
    val jobId: UUID,
    val subreddit: String,
    val pageNumber: Number,
    val maxPages: Number,
    val paginationToken: String?
)