package io.github.raissaamaral.mapadevagas.user;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;

@Service
public class UserService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);

        if (repository.existsByEmail(email)) {
            throw new EmailAlreadyRegisteredException();
        }

        String passwordHash = passwordEncoder.encode(request.password());
        User user = repository.save(new User(email, passwordHash, Instant.now()));

        return new UserResponse(user.getId(), user.getEmail());
    }
}