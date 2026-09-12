package com.example.arrayblockingqueue.queue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "queue.capacity=2")
class MessageQueueServiceTest {

    @Autowired
    private MessageQueueService service;

    @BeforeEach
    void setUp() {
        service.clear();
    }

    @Test
    void messagesAreConsumedInFifoOrder() throws InterruptedException {
        service.put("first");
        service.put("second");

        assertThat(service.take()).isEqualTo("first");
        assertThat(service.take()).isEqualTo("second");
    }

    @Test
    void clearRemovesAllMessages() throws InterruptedException {
        service.put("message");

        service.clear();

        assertThat(service.size()).isZero();
        assertThat(service.remainingCapacity()).isEqualTo(2);
    }
}
