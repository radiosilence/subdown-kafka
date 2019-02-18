package download

data class Download(
    val jobId: String,
    val postId: String,
    val url: String,
    val postedOn: String
)