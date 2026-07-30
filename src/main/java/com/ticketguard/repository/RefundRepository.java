package com.ticketguard.repository;

import com.ticketguard.entity.Refund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefundRepository extends JpaRepository<Refund, Long> {
    Optional<Refund> findByPaymentId(Long paymentId);
}
