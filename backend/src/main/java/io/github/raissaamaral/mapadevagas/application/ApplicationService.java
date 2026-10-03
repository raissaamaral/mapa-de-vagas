package io.github.raissaamaral.mapadevagas.application;

import io.github.raissaamaral.mapadevagas.user.UserRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Service
public class ApplicationService {

    private final ApplicationRepository repository;
    private final StatusHistoryRepository historyRepository;
    private final UserRepository userRepository;

    public ApplicationService(ApplicationRepository repository,
                              StatusHistoryRepository historyRepository,
                              UserRepository userRepository) {
        this.repository = repository;
        this.historyRepository = historyRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ApplicationResponse create(Long userId, ApplicationRequest request) {
        Application application = new Application();
        applyRequest(application, request);
        application.setOwner(userRepository.getReferenceById(userId));

        // Status is always SAVED on creation. It only changes through the
        // status endpoint, which records the history used by the metrics
        application.setStatus(ApplicationStatus.SAVED);
        Application saved = repository.save(application);
        recordHistory(saved);
        return toResponse(saved);
    }

    public ApplicationResponse findById(Long userId, Long id) {
        return toResponse(findApplicationOrThrow(userId, id));
    }

    public List<ApplicationResponse> findAll(Long userId, ApplicationStatus status,
                                             JobSource source, WorkModel workModel) {
        Specification<Application> spec = belongsTo(userId);
        if (status != null) {
            spec = spec.and(hasValue("status", status));
        }
        if (source != null) {
            spec = spec.and(hasValue("source", source));
        }
        if (workModel != null) {
            spec = spec.and(hasValue("workModel", workModel));
        }

        return repository.findAll(spec, Sort.by(Sort.Direction.DESC, "id"))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<ApplicationResponse> findSaved(Long userId) {
        return repository.findByOwnerIdAndStatusOrderByApplicationDeadlineAscIdDesc(userId, ApplicationStatus.SAVED)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ApplicationResponse update(Long userId, Long id, ApplicationRequest request) {
        Application application = findApplicationOrThrow(userId, id);
        applyRequest(application, request);
        Application saved = repository.save(application);
        return toResponse(saved);
    }

    public void delete(Long userId, Long id) {
        Application application = findApplicationOrThrow(userId, id);
        repository.delete(application);
    }

    @Transactional
    public ApplicationResponse changeStatus(Long userId, Long id, StatusChangeRequest request) {
        Application application = findApplicationOrThrow(userId, id);
        ApplicationStatus newStatus = request.status();

        if (application.getStatus() == newStatus) {
            throw new InvalidStatusChangeException(newStatus);
        }

        application.setStatus(newStatus);

        if (newStatus == ApplicationStatus.APPLIED && application.getAppliedOn() == null) {
            application.setAppliedOn(LocalDate.now());
        }

        Application saved = repository.save(application);
        recordHistory(saved);
        return toResponse(saved);
    }

    public List<StatusHistoryResponse> findHistory(Long userId, Long id) {
        // Goes through the owner-aware lookup: another user's application
        // returns 404 before any history is read
        findApplicationOrThrow(userId, id);

        return historyRepository.findByApplicationIdOrderByChangedAtAsc(id)
                .stream()
                .map(history -> new StatusHistoryResponse(history.getStatus(), history.getChangedAt()))
                .toList();
    }

    private Application findApplicationOrThrow(Long userId, Long id) {
        // The owner is part of the query: another user's application is simply
        // not found, so it returns 404 and its existence is not revealed
        return repository.findByIdAndOwnerId(id, userId)
                .orElseThrow(() -> new ApplicationNotFoundException(id));
    }

    private static Specification<Application> belongsTo(Long userId) {
        return (root, query, cb) -> cb.equal(root.get("owner").get("id"), userId);
    }

    private static Specification<Application> hasValue(String field, Object value) {
        return (root, query, cb) -> cb.equal(root.get(field), value);
    }

    private void recordHistory(Application application) {
        historyRepository.save(new StatusHistory(application, application.getStatus(), Instant.now()));
    }

    private ApplicationResponse toResponse(Application application) {
        return new ApplicationResponse(
                application.getId(),
                application.getCompany(),
                application.getJobTitle(),
                application.getJobUrl(),
                application.getSource(),
                application.getWorkModel(),
                application.getSeniority(),
                application.getLocation(),
                application.getSalary(),
                application.getTechStack(),
                application.getJobDescription(),
                application.getStatus(),
                application.getApplicationDeadline(),
                application.getAppliedOn(),
                application.getTotalStages(),
                application.getNotes()
        );
    }

    private void applyRequest(Application application, ApplicationRequest request) {
        application.setCompany(request.company());
        application.setJobTitle(request.jobTitle());
        application.setJobUrl(request.jobUrl());
        application.setSource(request.source());
        application.setWorkModel(request.workModel());
        application.setSeniority(request.seniority());
        application.setLocation(request.location());
        application.setSalary(request.salary());
        application.setTechStack(request.techStack());
        application.setJobDescription(request.jobDescription());
        application.setApplicationDeadline(request.applicationDeadline());
        application.setAppliedOn(request.appliedOn());
        application.setTotalStages(request.totalStages());
        application.setNotes(request.notes());
    }
}