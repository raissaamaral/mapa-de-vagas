package io.github.raissaamaral.mapadevagas.application;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long>,
        JpaSpecificationExecutor<Application> {

    Optional<Application> findByIdAndOwnerId(Long id, Long ownerId);

    List<Application> findByOwnerIdAndStatusOrderByApplicationDeadlineAscIdDesc(
            Long ownerId, ApplicationStatus status);
}