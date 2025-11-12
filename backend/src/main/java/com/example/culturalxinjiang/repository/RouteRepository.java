package com.example.culturalxinjiang.repository;

import com.example.culturalxinjiang.entity.Route;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {
    Page<Route> findByTheme(String theme, Pageable pageable);
}






