# Questions

Things I do not understand or need clarity on.

1. **Q:** Does my project layout look reasonable?

   **A:**

2. **Q:** How do I define an entry point for each "microservice"?

   **A:**

3. **Q:** How can I have something both start a Ratpack HTTP server and also initialise a kafka connection and post things to it?

   **A:**

4. **Q:** Do I have to define an object for every single JSON response with Ratpack?

   **A:**

5. **Q:** Can I declare a kafka setup (topics etc) in a config file and have it spun up as opposed to doing this manually?

   **A:**

6. **Q:** How does Java's concurrency model work? I assume threads, but are these created transparently when you for instance, consumer something from kafka, or does one process block - i.e. if my downloader consumer polls kafka and starts downloading something will it block until that is downloaded?

   **A:**

7. **Q:** Can I use kafka as a task queue in such a way that consuming a message from it kills it for other things - I am guessing no, but in that case could I use redis to mark a job as consumed if I wanted to spin up multiple "downloader processes" to distribute work? Or is Kafka really not the right tool for work distribution?

   **A:**
