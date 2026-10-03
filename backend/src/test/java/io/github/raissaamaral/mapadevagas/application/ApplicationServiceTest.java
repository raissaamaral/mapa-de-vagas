package io.github.raissaamaral.mapadevagas.application;

import io.github.raissaamaral.mapadevagas.user.User;
import io.github.raissaamaral.mapadevagas.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    private static final Long USER_ID = 1L;

    @Mock
    private ApplicationRepository repository;

    @Mock
    private StatusHistoryRepository historyRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ApplicationService service;

    @Test
    void createShouldAlwaysSetStatusToSaved() {
        when(userRepository.getReferenceById(USER_ID)).thenReturn(owner());
        when(repository.save(any(Application.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ApplicationResponse response = service.create(USER_ID, request());

        assertEquals(ApplicationStatus.SAVED, response.status());
    }

    @Test
    void createShouldRecordInitialHistoryEntry() {
        when(userRepository.getReferenceById(USER_ID)).thenReturn(owner());
        when(repository.save(any(Application.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.create(USER_ID, request());

        verify(historyRepository).save(argThat(history -> history.getStatus() == ApplicationStatus.SAVED));
    }

    @Test
    void createShouldSetTheAuthenticatedUserAsOwner() {
        User owner = owner();
        when(userRepository.getReferenceById(USER_ID)).thenReturn(owner);
        when(repository.save(any(Application.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.create(USER_ID, request());

        verify(repository).save(argThat(application -> application.getOwner() == owner));
    }

    @Test
    void findByIdShouldThrowWhenApplicationIsNotFoundForUser() {
        when(repository.findByIdAndOwnerId(999L, USER_ID)).thenReturn(Optional.empty());

        assertThrows(ApplicationNotFoundException.class, () -> service.findById(USER_ID, 999L));
    }

    @Test
    void updateShouldNotChangeStatus() {
        Application existing = new Application();
        existing.setStatus(ApplicationStatus.APPLIED);
        when(repository.findByIdAndOwnerId(1L, USER_ID)).thenReturn(Optional.of(existing));
        when(repository.save(any(Application.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ApplicationResponse response = service.update(USER_ID, 1L, request());

        assertEquals(ApplicationStatus.APPLIED, response.status());
    }

    @Test
    void changeStatusShouldThrowWhenStatusIsUnchanged() {
        Application existing = new Application();
        existing.setStatus(ApplicationStatus.APPLIED);
        when(repository.findByIdAndOwnerId(1L, USER_ID)).thenReturn(Optional.of(existing));

        StatusChangeRequest request = new StatusChangeRequest(ApplicationStatus.APPLIED);

        assertThrows(InvalidStatusChangeException.class, () -> service.changeStatus(USER_ID, 1L, request));
        verify(historyRepository, never()).save(any());
    }

    @Test
    void changeStatusShouldSetAppliedOnWhenStatusBecomesApplied() {
        Application existing = new Application();
        existing.setStatus(ApplicationStatus.SAVED);
        when(repository.findByIdAndOwnerId(1L, USER_ID)).thenReturn(Optional.of(existing));
        when(repository.save(any(Application.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ApplicationResponse response = service.changeStatus(
                USER_ID, 1L, new StatusChangeRequest(ApplicationStatus.APPLIED));

        assertEquals(LocalDate.now(), response.appliedOn());
    }

    private static User owner() {
        return new User("rai@example.com", "hash", Instant.now());
    }

    private static ApplicationRequest request() {
        return new ApplicationRequest(
                "Rai Corp", "Estágio", null, null, null, null, null,
                null, null, null, null, null, null, null);
    }
}