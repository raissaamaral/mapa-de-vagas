package io.github.raissaamaral.mapadevagas.application;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/applications")
public class ApplicationController {

    private final ApplicationService service;

    public ApplicationController(ApplicationService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ApplicationResponse> create(@AuthenticationPrincipal Jwt jwt,
                                                      @Valid @RequestBody ApplicationRequest request) {
        ApplicationResponse response = service.create(userId(jwt), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ApplicationResponse>> findAll(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) ApplicationStatus status,
            @RequestParam(required = false) JobSource source,
            @RequestParam(required = false) WorkModel workModel) {
        return ResponseEntity.ok(service.findAll(userId(jwt), status, source, workModel));
    }

    @GetMapping("/saved")
    public ResponseEntity<List<ApplicationResponse>> findSaved(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(service.findSaved(userId(jwt)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApplicationResponse> findById(@AuthenticationPrincipal Jwt jwt,
                                                        @PathVariable Long id) {
        return ResponseEntity.ok(service.findById(userId(jwt), id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApplicationResponse> update(@AuthenticationPrincipal Jwt jwt,
                                                      @PathVariable Long id,
                                                      @Valid @RequestBody ApplicationRequest request) {
        return ResponseEntity.ok(service.update(userId(jwt), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        service.delete(userId(jwt), id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApplicationResponse> changeStatus(@AuthenticationPrincipal Jwt jwt,
                                                            @PathVariable Long id,
                                                            @Valid @RequestBody StatusChangeRequest request) {
        return ResponseEntity.ok(service.changeStatus(userId(jwt), id, request));
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<StatusHistoryResponse>> findHistory(@AuthenticationPrincipal Jwt jwt,
                                                                   @PathVariable Long id) {
        return ResponseEntity.ok(service.findHistory(userId(jwt), id));
    }

    private static Long userId(Jwt jwt) {
        // The owner always comes from the validated token, never from the request
        return Long.valueOf(jwt.getSubject());
    }
}