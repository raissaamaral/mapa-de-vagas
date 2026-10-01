package io.github.raissaamaral.mapadevagas.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock
    private ApplicationRepository repository;

    @Mock
    private StatusHistoryRepository historyRepository;

    @InjectMocks
    private ApplicationService service;

    @Test
    void createShouldAlwaysSetStatusToSaved() {
        // Arrange: the mock returns the same entity it receives
        when(repository.save(any(Application.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ApplicationRequest request = new ApplicationRequest(
                "Rai Corp", "Estágio", null, null, null, null, null,
                null, null, null, null, null, null, null);

        // Act
        ApplicationResponse response = service.create(request);

        // Assert
        assertEquals(ApplicationStatus.SAVED, response.status());
    }

    @Test
    void findByIdShouldThrowWhenApplicationDoesNotExist() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ApplicationNotFoundException.class, () -> service.findById(999L));
    }

    @Test
    void updateShouldNotChangeStatus() {
        Application existing = new Application();
        existing.setStatus(ApplicationStatus.APPLIED);

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(Application.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ApplicationRequest request = new ApplicationRequest(
                "Rai Corp", "Estágio", null, null, null, null, null,
                null, null, null, null, null, null, null);

        ApplicationResponse response = service.update(1L, request);

        assertEquals(ApplicationStatus.APPLIED, response.status());
    }

    @Test
    void createShouldRecordInitialHistoryEntry() {
        when(repository.save(any(Application.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ApplicationRequest request = new ApplicationRequest(
                "Rai Corp", "Estágio", null, null, null, null, null,
                null, null, null, null, null, null, null);

        service.create(request);

        verify(historyRepository).save(argThat(history -> history.getStatus() == ApplicationStatus.SAVED));
    }

    @Test
    void changeStatusShouldThrowWhenStatusIsUnchanged() {
        Application existing = new Application();
        existing.setStatus(ApplicationStatus.APPLIED);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        StatusChangeRequest request = new StatusChangeRequest(ApplicationStatus.APPLIED);

        assertThrows(InvalidStatusChangeException.class, () -> service.changeStatus(1L, request));
        verify(historyRepository, never()).save(any());
    }

    @Test
    void changeStatusShouldSetAppliedOnWhenStatusBecomesApplied() {
        Application existing = new Application();
        existing.setStatus(ApplicationStatus.SAVED);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(Application.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ApplicationResponse response = service.changeStatus(1L, new StatusChangeRequest(ApplicationStatus.APPLIED));

        assertEquals(LocalDate.now(), response.appliedOn());
    }
}