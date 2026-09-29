package io.github.raissaamaral.mapadevagas.application;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/applications")
public class ApplicationController {

    private final ApplicationService service;

    public ApplicationController(ApplicationService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ApplicationResponse> create(@Valid @RequestBody ApplicationRequest request) {
        ApplicationResponse response = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApplicationResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

}
