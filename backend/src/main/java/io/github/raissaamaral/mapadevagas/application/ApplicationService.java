package io.github.raissaamaral.mapadevagas.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Service
public class ApplicationService {

    private final ApplicationRepository repository;
    private final StatusHistoryRepository historyRepository;

    public ApplicationService(ApplicationRepository repository,
                              StatusHistoryRepository historyRepository) {
        this.repository = repository;
        this.historyRepository = historyRepository;
    }

    @Transactional
    public ApplicationResponse create(ApplicationRequest request) {
        Application application = new Application();
        applyRequest(application, request);

        // Status is always SAVED on creation. It only changes through the
        // status endpoint, which records the history used by the metrics
        application.setStatus(ApplicationStatus.SAVED);
        Application saved = repository.save(application);
        recordHistory(saved);
        return toResponse(saved);
    }

    public ApplicationResponse findById(Long id) {
        Application application = findApplicationOrThrow(id);
        return toResponse(application);
    }

    public List<ApplicationResponse> findAll() {
        return repository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ApplicationResponse update(Long id, ApplicationRequest request) {
        Application application = findApplicationOrThrow(id);
        applyRequest(application, request);
        Application saved = repository.save(application);
        return toResponse(saved);
    }

    public void delete(Long id) {
        Application application = findApplicationOrThrow(id);
        repository.delete(application);
    }

    @Transactional
    public ApplicationResponse changeStatus(Long id, StatusChangeRequest request) {
        Application application = findApplicationOrThrow(id);
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

    public List<StatusHistoryResponse> findHistory(Long id) {
        // Goes through the same lookup as every endpoint: returns 404 for
        // unknown ids and will get the ownership check with authentication
        findApplicationOrThrow(id);

        return historyRepository.findByApplicationIdOrderByChangedAtAsc(id)
                .stream()
                .map(history -> new StatusHistoryResponse(history.getStatus(), history.getChangedAt()))
                .toList();
    }

    private void recordHistory(Application application) {
        historyRepository.save(new StatusHistory(application, application.getStatus(), Instant.now()));
    }

    private Application findApplicationOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ApplicationNotFoundException(id));
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