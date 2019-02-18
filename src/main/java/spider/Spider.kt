package spider

data class Spider(
    val jobId: String,
    val subreddit: String,
    val pageNumber: Number,
    val maxPages: Number,
    val paginationToken: String
)