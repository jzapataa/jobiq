package com.jobiq.auth.service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jobiq.auth.dto.AuthMeResponse;
import com.jobiq.auth.dto.LoginRequest;
import com.jobiq.auth.dto.LoginResponse;
import com.jobiq.auth.dto.RegisterRequest;
import com.jobiq.auth.exception.AuthValidationException;
import com.jobiq.auth.exception.EmailAlreadyExistsException;
import com.jobiq.auth.exception.InvalidCredentialsException;
import com.jobiq.auth.exception.UnauthenticatedException;
import com.jobiq.auth.security.JwtService;
import com.jobiq.auth.security.JwtService.IssuedAccessToken;
import com.jobiq.candidates.service.CandidateProfileStatusService;
import com.jobiq.recruiters.service.RecruiterProfileStatusService;
import com.jobiq.shared.error.ApiFieldError;
import com.jobiq.users.domain.User;
import com.jobiq.users.domain.UserRole;
import com.jobiq.users.repository.UserRepository;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CandidateProfileStatusService candidateProfileStatusService;
    private final RecruiterProfileStatusService recruiterProfileStatusService;
    private final Clock clock;
    private final Validator validator;
    private final String dummyPasswordHash;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            CandidateProfileStatusService candidateProfileStatusService,
            RecruiterProfileStatusService recruiterProfileStatusService,
            Clock clock,
            Validator validator) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.candidateProfileStatusService = candidateProfileStatusService;
        this.recruiterProfileStatusService = recruiterProfileStatusService;
        this.clock = clock;
        this.validator = validator;
        this.dummyPasswordHash = passwordEncoder.encode("jobiq-auth-timing-placeholder");
    }

    @Transactional
    public void register(RegisterRequest request) {
        String email = normalizeAndValidateEmail(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException();
        }

        Instant now = clock.instant();
        User user = User.create(
                UUID.randomUUID(), email, passwordEncoder.encode(request.password()), request.name().trim(), request.role(), now);

        try {
            userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException exception) {
            throw new EmailAlreadyExistsException();
        }
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        String email = normalizeAndValidateEmail(request.email());
        User user = userRepository.findByEmail(email).orElse(null);
        String passwordHash = user == null ? dummyPasswordHash : user.getPasswordHash();
        boolean matches = passwordEncoder.matches(request.password(), passwordHash);

        if (user == null || !matches) {
            throw new InvalidCredentialsException();
        }

        IssuedAccessToken token = jwtService.issue(user);
        return new LoginResponse(token.value(), "Bearer", token.expiresAt());
    }

    @Transactional(readOnly = true)
    public AuthMeResponse currentUser(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(UnauthenticatedException::new);
        boolean profileComplete = switch (user.getRole()) {
            case CANDIDATE -> candidateProfileStatusService.existsForUser(userId);
            case RECRUITER -> recruiterProfileStatusService.existsForUser(userId);
        };
        return new AuthMeResponse(user.getId(), user.getEmail(), user.getName(), user.getRole(), profileComplete);
    }

    private String normalizeAndValidateEmail(String rawEmail) {
        String normalized = rawEmail == null ? "" : rawEmail.trim().toLowerCase(Locale.ROOT);
        Set<ConstraintViolation<NormalizedEmail>> violations = validator.validate(new NormalizedEmail(normalized));
        if (!violations.isEmpty()) {
            throw new AuthValidationException(List.of(new ApiFieldError("email", "must be a well-formed email address")));
        }
        return normalized;
    }

    private record NormalizedEmail(@NotBlank @Email @Size(max = 320) String value) {
    }
}
