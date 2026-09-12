package com.example.arrayblockingqueue.queue;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QueueMessageRepository extends JpaRepository<QueueMessage, Long> {
    QueueMessage findFirstByOrderByIdAsc();
}
