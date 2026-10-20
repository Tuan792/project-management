
package com.taskflow.api.auth;

import java.util.Locale;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.taskflow.api.security.JwtService;
import com.taskflow.api.user.AppUser;
import com.taskflow.api.user.UserRepository;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthController(
            UserRepository users,
            PasswordEncoder encoder,
            JwtService jwt
    ) {
        this.users = users;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    public record RegisterRequest(
            @NotBlank @Size(max = 120) String fullName,
            @NotBlank @Email String email,
            @NotBlank @Size(min = 8, max = 72) String password
    ) {
    }

    public record LoginRequest(
            @NotBlank @Email String email,
            @NotBlank String password
    ) {
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        String email = request.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        if (users.existsByEmailIgnoreCase(email)) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "message",
                            "Email đã được sử dụng"
                    ));
        }

        AppUser user = users.save(
                new AppUser(
                        request.fullName().trim(),
                        email,
                        encoder.encode(request.password())
                )
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Map.of(
                        "token", jwt.generate(user.getEmail()),
                        "user", Map.of(
                                "id", user.getId(),
                                "fullName", user.getFullName(),
                                "email", user.getEmail()
                        )
                ));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest request
    ) {
        var user = users
                .findByEmailIgnoreCase(request.email().trim())
                .orElse(null);

        if (user == null
                || !encoder.matches(
                        request.password(),
                        user.getPasswordHash()
                )) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "message",
                            "Email hoặc mật khẩu không chính xác"
                    ));
        }

        return ResponseEntity.ok(
                Map.of(
                        "token", jwt.generate(user.getEmail()),
                        "user", Map.of(
                                "id", user.getId(),
                                "fullName", user.getFullName(),
                                "email", user.getEmail()
                        )
                )
        );
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        var user = users
                .findByEmailIgnoreCase(authentication.getName())
                .orElseThrow();

        return ResponseEntity.ok(
                Map.of(
                        "id", user.getId(),
                        "fullName", user.getFullName(),
                        "email", user.getEmail()
                )
        );
    }
}
