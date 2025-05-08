package com.bits.borrowservice.repository;

import com.bits.borrowservice.entity.BorrowExtension;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface BorrowExtensionRepository extends JpaRepository<BorrowExtension, Long> {
    @Query("SELECT COUNT(be) FROM BorrowExtension be WHERE be.borrow.id = :borrowId")
    long countExtensionsByBorrowId(Long borrowId);
} 