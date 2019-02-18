# subdown-kafka

Project will be an API to download images from subreddits massively in parallel.

This is a port of my existing project that I use to learn languages:

https://github.com/radiosilence/subdown

## Architecture

### HTTP API

Endpoints

- GET /status - reports current jobs, status, what it's doing, etc.
- POST /spider - takes HTTP body `{ subreddit: string, maxPages: number }`
  When this API is hit, it sends a message to the `spider` topic with format:
  { subreddit, maxPages, paginationToken (null in this case)}

### Consumer for `spider` topic

1. Consumes messages from `spider`
2. Does HTTP request to get the JSON
3. if `pageNumber < maxPages`, posts message to `spider` with next page details
4. For each link, posts message to `download` with the URL for that link

### Consumer for `download` topic

1. Consumes messages from `download`
2. Checks file doesn't exist with postId
3. Downloads URL
4. Sets downloaded file to have the modified time of the postedOn

## Topics / Messages

`spider`

```ts
{
  subreddit: string;
  pageNumber: number;
  maxPages: number;
  paginationToken?: string
}
```

`download`

```ts
{
  postId: string;
  url: string;
  postedOn: string;
}
```
