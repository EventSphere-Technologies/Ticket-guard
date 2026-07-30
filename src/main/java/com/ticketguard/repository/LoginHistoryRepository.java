package com.ticketguard.repository;

import com.ticketguard.entity.LoginHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long> {
    List<LoginHistory> findByUserIdOrderByLoginTimeDesc(Long userId);
    List<LoginHistory> findByIpAddress(String ipAddress);
}
