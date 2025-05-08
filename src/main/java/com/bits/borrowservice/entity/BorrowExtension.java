package com.bits.borrowservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "borrow_extensions")
public class BorrowExtension {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "borrow_id", nullable = false)
    private Borrow borrow;

    @Column(name = "extended_days", nullable = false)
    private Integer extendedDays;

    @Column(name = "new_due_date", nullable = false)
    private LocalDateTime newDueDate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
} 