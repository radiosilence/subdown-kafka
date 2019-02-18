# subdown-kafka

Project will be an API to download images from subreddits in parallel using Java/Kotlin and Kafka

This is a port of my existing project that I use to learn languages, but a bit more hardcore:

https://github.com/radiosilence/subdown

## Current questions

[Here are a bunch of questions I have](https://github.com/radiosilence/subdown-kafka/blob/master/QUESTIONS.md)

## Skills to learn

- How to set up an sort of Java/Kotlin project at all
  - Gradle
  - Kotlin
  - Project structure
- Setting up kafka
- HTTP API making
- JSON parsing
- Posting to Kafka
- Consuming from Kafka
- File system modification
- Redis

## Architecture

### HTTP API (Ratpack)

#### Endpoints

- GET /status - Queries redis to show statuses of last whatever jobs and returns JSON
- POST /spider - takes HTTP body `{ subreddit: string, maxPages: number }`
  When this API is hit, it sends a message to the `spider` topic with format:
  { subreddit, maxPages, paginationToken (null in this case)}

### Consumer for `spider` topic

1. Consumes messages from `spider`
2. Sends status for job as spidering
3. Does HTTP request to get the JSON
4. if `pageNumber < maxPages`, posts message to `spider` with next page details
5. Sends status for job as complete
6. For each link, posts message to `download` with the URL for that link

### Consumer for `download` topic

1. Consumes messages from `download`
2. Sends status for job as downloading
3. Checks file doesn't exist with postId
4. Downloads URL
5. Sends status for job as complete
6. Sets downloaded file to have the modified time of the postedOn

### Consumer for `status` topic

1. Consumes messages from `status` topic
2. If `job:{id}` does not exist, LPUSH jobId into `jobs`
3. Set `job:{id}` to JSON dump of the status

## Topics / Messages

`spider`

```ts
{
  jobId: string
  subreddit: string;
  pageNumber: number;
  maxPages: number;
  paginationToken?: string
}
```

`download`

```ts
{
  jobId: string;
  postId: string;
  url: string;
  postedOn: string;
}
```

`status`

```ts
{
  jobId: string;
  jobType: "DOWNLOAD" | "SPIDER";
  jobDescription: string;
  status: string;
}
```
