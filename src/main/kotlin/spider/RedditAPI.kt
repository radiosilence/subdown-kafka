package spider

import com.fasterxml.jackson.annotation.JsonIgnoreProperties


@JsonIgnoreProperties(ignoreUnknown = true)
data class Link(
    val id: String,
    val url: String,
    val created_utc: Number
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SubredditChild(
    val kind: String,
    val data: Link
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SubredditData(
    val children: Array<SubredditChild>,
    val before: String?,
    val after: String?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SubredditResponse(
    val kind: String,
    val data: SubredditData
)