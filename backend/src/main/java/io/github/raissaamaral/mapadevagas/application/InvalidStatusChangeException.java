package io.github.raissaamaral.mapadevagas.application;

public class InvalidStatusChangeException extends RuntimeException {
    public InvalidStatusChangeException(ApplicationStatus status) {
        super("Application is already in status " + status);
    }
}