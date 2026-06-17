package com.example.payments.infrastructure.adapters.out.persistence;
import org.springframework.data.jpa.repository.JpaRepository;import java.util.*;
public interface OutboxJpaRepository extends JpaRepository<OutboxEntity, UUID> { List<OutboxEntity> findTop50ByPublishedFalseOrderByCreatedAtAsc(); }
