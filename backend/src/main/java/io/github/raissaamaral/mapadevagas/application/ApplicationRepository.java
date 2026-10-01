package io.github.raissaamaral.mapadevagas.application;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findByStatusOrderByApplicationDeadlineAscIdDesc(ApplicationStatus status);
}
