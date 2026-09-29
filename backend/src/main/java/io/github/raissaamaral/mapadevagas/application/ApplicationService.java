package io.github.raissaamaral.mapadevagas.application;

import org.springframework.stereotype.Service;

@Service
public class ApplicationService {

    private final ApplicationRepository repository;

    public ApplicationService(ApplicationRepository repository) {
        this.repository = repository;
    }

    public ApplicationResponse create(ApplicationRequest request) {

        Application application = new Application();
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

        // Status is always SAVED on creation. It only changes through the
        // status endpoint, which records the history used by the metrics
        application.setStatus(ApplicationStatus.SAVED);
        Application saved = repository.save(application);

        return toResponse(saved);
    }

    public ApplicationResponse findById(Long id) {
        Application application = repository.findById(id)
                .orElseThrow(() -> new ApplicationNotFoundException(id));
        return toResponse(application);
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
}
