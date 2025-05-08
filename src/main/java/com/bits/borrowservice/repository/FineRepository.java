package com.bits.borrowservice.repository;

import com.bits.borrowservice.entity.Fine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FineRepository extends JpaRepository<Fine, Long> {
    Page<Fine> findByBorrowUserId(Long userId, Pageable pageable);
    
    Page<Fine> findByStatus(Fine.FineStatus status, Pageable pageable);
} 