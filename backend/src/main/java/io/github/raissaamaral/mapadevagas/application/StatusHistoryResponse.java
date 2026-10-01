package io.github.raissaamaral.mapadevagas.application;

import java.time.Instant;

public record StatusHistoryResponse(ApplicationStatus status, Instant changedAt) {
}