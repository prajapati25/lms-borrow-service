package com.bits.borrowservice.repository;

import com.bits.borrowservice.entity.Borrow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BorrowRepository extends JpaRepository<Borrow, Long> {

    Page<Borrow> findByUserId(Long userId, Pageable pageable);
    
    Page<Borrow> findByBookId(Long bookId, Pageable pageable);
    
    @Query("SELECT b FROM Borrow b WHERE b.status = 'BORROWED' AND b.dueDate < :now")
    List<Borrow> findOverdueBorrows(@Param("now") LocalDateTime now);
    
    @Query("SELECT b FROM Borrow b WHERE b.status = 'BORROWED' AND b.dueDate < :now")
    Page<Borrow> findOverdueBorrows(@Param("now") LocalDateTime now, Pageable pageable);
    
    @Query("SELECT COUNT(b) FROM Borrow b WHERE b.userId = :userId AND b.status = 'BORROWED'")
    long countActiveBorrowsByUserId(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(b) FROM Borrow b WHERE b.bookId = :bookId AND b.status = 'BORROWED'")
    long countActiveBorrowsByBookId(@Param("bookId") Long bookId);
} 