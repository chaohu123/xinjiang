package com.example.culturalxinjiang.repository;

import com.example.culturalxinjiang.entity.EventRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Long> {
    Optional<EventRegistration> findByUserIdAndEventId(Long userId, Long eventId);
    boolean existsByUserIdAndEventId(Long userId, Long eventId);
    long countByEventId(Long eventId);
    List<EventRegistration> findByEventIdOrderByCreatedAtAsc(Long eventId);
    void deleteByEventId(Long eventId);
}





