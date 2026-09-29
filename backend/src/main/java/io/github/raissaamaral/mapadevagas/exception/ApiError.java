package io.github.raissaamaral.mapadevagas.exception;

import java.util.Map;

public record ApiError(int status, String message, Map<String, String> errors) {
}
