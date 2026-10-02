package io.github.raissaamaral.mapadevagas.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Email @Size(max = 254) String email,
        @NotBlank @Size(min = 8, max = 64) String password) {

    // Records print every field in toString(); the password must never
    // appear in logs, so it is masked here
    @Override
    public String toString() {
        return "RegisterRequest[email=" + email + ", password=***]";
    }
}