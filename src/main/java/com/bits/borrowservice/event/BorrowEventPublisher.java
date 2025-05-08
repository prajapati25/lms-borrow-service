package com.bits.borrowservice.event;

import com.bits.borrowservice.entity.Borrow;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BorrowEventPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishBorrowEvent(Borrow borrow) {
        kafkaTemplate.send("borrow-events", borrow.getId().toString(), borrow);
    }

    public void publishReturnEvent(Borrow borrow) {
        kafkaTemplate.send("return-events", borrow.getId().toString(), borrow);
    }

    public void publishDueDateEvent(Borrow borrow) {
        kafkaTemplate.send("due-date-events", borrow.getId().toString(), borrow);
    }
} 