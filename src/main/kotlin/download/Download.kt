package download

import java.util.*

data class Download(
    val jobId: UUID,
    val postId: String,
    val url: String?,
    val postedOn: Date
    )