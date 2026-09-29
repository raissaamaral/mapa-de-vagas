package io.github.raissaamaral.mapadevagas.application;

public class ApplicationNotFoundException extends RuntimeException {
    public ApplicationNotFoundException(Long id) {
        super("Application not found: " + id);
    }
}
