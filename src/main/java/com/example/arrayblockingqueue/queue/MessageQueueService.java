package com.example.arrayblockingqueue.queue;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Service
public class MessageQueueService {

    private final QueueMessageRepository repository;
    private final int capacity;
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition notEmpty = lock.newCondition();
    private final Condition notFull = lock.newCondition();

    public MessageQueueService(QueueMessageRepository repository, @Value("${queue.capacity:10}") int capacity) {
        if (capacity < 1) {
            throw new IllegalArgumentException("queue.capacity must be at least 1");
        }
        this.repository = repository;
        this.capacity = capacity;
        log.info("Initialized MessageQueueService with capacity: {}", capacity);
    }

    @Transactional
    public void put(String message) throws InterruptedException {
        lock.lockInterruptibly();
        try {
            while (repository.count() >= capacity) {
                log.info("Queue is full, waiting to put message: {}", message);
                notFull.await();
            }
            QueueMessage saved = repository.save(new QueueMessage(message));
            log.info("Put message to queue (id: {}): {}", saved.getId(), message);
            notEmpty.signal();
        } finally {
            lock.unlock();
        }
    }

    @Transactional
    public String take() throws InterruptedException {
        lock.lockInterruptibly();
        try {
            QueueMessage first;
            while ((first = repository.findFirstByOrderByIdAsc()) == null) {
                log.info("Queue is empty, waiting to take message");
                notEmpty.await();
            }
            repository.delete(first);
            log.info("Took message from queue (id: {}): {}", first.getId(), first.getMessage());
            notFull.signal();
            return first.getMessage();
        } finally {
            lock.unlock();
        }
    }

    public int size() {
        lock.lock();
        try {
            return (int) repository.count();
        } finally {
            lock.unlock();
        }
    }

    public int remainingCapacity() {
        return capacity - size();
    }

    public int capacity() {
        return capacity;
    }

    @Transactional
    public void clear() {
        lock.lock();
        try {
            long count = repository.count();
            repository.deleteAllInBatch();
            log.info("Cleared queue. Removed {} messages.", count);
            notFull.signalAll();
        } finally {
            lock.unlock();
        }
    }
}
