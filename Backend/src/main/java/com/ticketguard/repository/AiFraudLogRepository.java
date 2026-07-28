package com.ticketguard.repository;

import com.ticketguard.entity.AiFraudLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiFraudLogRepository extends JpaRepository<AiFraudLog, Long> {
    List<AiFraudLog> findByOrderByCreatedAtDesc();
}
