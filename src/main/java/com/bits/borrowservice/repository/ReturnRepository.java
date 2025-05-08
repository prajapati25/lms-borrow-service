package com.bits.borrowservice.repository;

import com.bits.borrowservice.entity.Return;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
 
@Repository
public interface ReturnRepository extends JpaRepository<Return, Long> {
    boolean existsByBorrowId(Long borrowId);
} 