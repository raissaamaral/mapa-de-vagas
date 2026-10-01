package io.github.raissaamaral.mapadevagas.application;

import jakarta.validation.constraints.NotNull;

public record StatusChangeRequest(@NotNull ApplicationStatus status) {
}