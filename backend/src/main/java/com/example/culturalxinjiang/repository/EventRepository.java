package com.example.culturalxinjiang.repository;

import com.example.culturalxinjiang.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    @Query("SELECT e FROM Event e WHERE " +
           "(:type IS NULL OR e.type = :type) AND " +
           "(:month IS NULL OR FUNCTION('YEAR', e.startDate) = FUNCTION('YEAR', :month) AND " +
           "FUNCTION('MONTH', e.startDate) = FUNCTION('MONTH', :month))")
    Page<Event> findByTypeAndMonth(
            @Param("type") Event.EventType type,
            @Param("month") LocalDate month,
            Pageable pageable
    );

    List<Event> findByStatus(Event.EventStatus status);
    Page<Event> findByStatus(Event.EventStatus status, Pageable pageable);
    Page<Event> findByTitleContainingIgnoreCase(String keyword, Pageable pageable);
    Page<Event> findByTitleContainingIgnoreCaseAndStatus(String keyword, Event.EventStatus status, Pageable pageable);

    @Query("SELECT e FROM Event e WHERE " +
           "e.endDate >= :startDate AND e.startDate <= :endDate AND " +
           "e.status IN :statuses " +
           "ORDER BY e.startDate ASC")
    Page<Event> findRecentEvents(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("statuses") List<Event.EventStatus> statuses,
            Pageable pageable
    );
}





