package io.github.raissaamaral.mapadevagas.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository repository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private UserService service;

    @BeforeEach
    void setUp() {
        service = new UserService(repository, passwordEncoder);
    }

    @Test
    void registerShouldStoreHashNotPlainPassword() {
        when(repository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.register(new RegisterRequest("rai@example.com", "senha-forte-123"));

        verify(repository).save(argThat(user ->
                !user.getPasswordHash().equals("senha-forte-123")
                        && passwordEncoder.matches("senha-forte-123", user.getPasswordHash())));
    }

    @Test
    void registerShouldNormalizeEmail() {
        when(repository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.register(new RegisterRequest("  Rai@Example.COM ", "senha-forte-123"));

        verify(repository).save(argThat(user -> user.getEmail().equals("rai@example.com")));
    }

    @Test
    void registerShouldThrowWhenEmailAlreadyExists() {
        when(repository.existsByEmail("rai@example.com")).thenReturn(true);

        assertThrows(EmailAlreadyRegisteredException.class,
                () -> service.register(new RegisterRequest("rai@example.com", "senha-forte-123")));

        verify(repository, never()).save(any());
    }
}