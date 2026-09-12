package com.example.arrayblockingqueue.queue;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/messages")
public class MessageQueueController {

    private final MessageQueueService queueService;

    public MessageQueueController(MessageQueueService queueService) {
        this.queueService = queueService;
    }

    @PostMapping
    public EnqueueResponse enqueue(@RequestBody EnqueueRequest request) throws InterruptedException {
        String message = request.message();
        if (message == null || message.isBlank()) {
            log.warn("Enqueue failed: message is empty");
            throw new IllegalArgumentException("Message must not be blank");
        }

        log.info("Received request to enqueue message: {}", message);
        queueService.put(message);
        return new EnqueueResponse(message);
    }

    @GetMapping("/next")
    public DequeueResponse dequeue() throws InterruptedException {
        log.info("Received request to dequeue message");
        return new DequeueResponse(queueService.take());
    }

    @GetMapping("/status")
    public StatusResponse status() {
        return new StatusResponse(
                queueService.size(),
                queueService.capacity(),
                queueService.remainingCapacity());
    }

    @DeleteMapping
    public void clear() {
        log.info("Received request to clear the queue");
        queueService.clear();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(new ErrorResponse(ex.getMessage()));
    }

    public record EnqueueRequest(String message) {}
    public record EnqueueResponse(String message) {}
    public record DequeueResponse(String message) {}
    public record StatusResponse(int size, int capacity, int remainingCapacity) {}
    public record ErrorResponse(String error) {}
}