# ArrayBlockingQueue

Spring Boot and Gradle sample project using Java's bounded, thread-safe `ArrayBlockingQueue`.

## Run

```bash
./gradlew bootRun
```

The server starts on `http://localhost:8080`.

## API

```bash
# Add a message
curl -X POST http://localhost:8080/api/messages \
  -H 'Content-Type: application/json' \
  -d '{"message":"hello"}'

# Consume the oldest message
curl http://localhost:8080/api/messages/next

# Check queue status
curl http://localhost:8080/api/messages/status

# Remove all queued messages
curl -X DELETE http://localhost:8080/api/messages
```

The queue capacity defaults to `10` and can be changed with `QUEUE_CAPACITY` or
`--queue.capacity=20`.

