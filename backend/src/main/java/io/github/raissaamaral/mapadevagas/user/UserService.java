package io.github.raissaamaral.mapadevagas.user;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final String dummyPasswordHash;

    public UserService(UserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        // Checked when the email does not exist, so a failed login takes the
        // same time either way and response times do not reveal registered emails
        this.dummyPasswordHash = passwordEncoder.encode("timing-protection-dummy");
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());

        if (repository.existsByEmail(email)) {
            throw new EmailAlreadyRegisteredException();
        }

        String passwordHash = passwordEncoder.encode(request.password());
        User user = repository.save(new User(email, passwordHash, Instant.now()));

        return toResponse(user);
    }

    public UserResponse authenticate(LoginRequest request) {
        Optional<User> user = repository.findByEmail(normalizeEmail(request.email()));

        String hash = user.map(User::getPasswordHash).orElse(dummyPasswordHash);
        boolean passwordMatches = passwordEncoder.matches(request.password(), hash);

        if (user.isEmpty() || !passwordMatches) {
            throw new InvalidCredentialsException();
        }

        return toResponse(user.get());
    }

    public UserResponse findById(Long id) {
        return repository.findById(id)
                .map(this::toResponse)
                .orElseThrow(InvalidCredentialsException::new);
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getEmail());
    }

    private static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}