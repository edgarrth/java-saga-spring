package com.example.payments.infrastructure.adapters.out.persistence;
import org.springframework.data.jpa.repository.JpaRepository;import java.util.*;
public interface EventStoreJpaRepository extends JpaRepository<EventStoreEntity, UUID> { List<EventStoreEntity> findByAggregateIdOrderByCreatedAtAsc(UUID aggregateId); }
