package io.github.raissaamaral.mapadevagas.application;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
public class StatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id")
    private Application application;

    @Enumerated(EnumType.STRING)
    private ApplicationStatus status;

    private Instant changedAt;

    protected StatusHistory() {
    }

    public StatusHistory(Application application, ApplicationStatus status, Instant changedAt) {
        this.application = application;
        this.status = status;
        this.changedAt = changedAt;
    }

    public Long getId() {
        return id;
    }

    public Application getApplication() {
        return application;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public Instant getChangedAt() {
        return changedAt;
    }
}