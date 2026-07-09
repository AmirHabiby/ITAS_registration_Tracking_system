package com.aronalvarenga.rtts.identity.application;

import com.aronalvarenga.rtts.identity.domain.UserAccount;
import com.aronalvarenga.rtts.identity.domain.UserAccountRepository;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserAccountRepository userAccountRepository, PasswordEncoder passwordEncoder) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserAccount authenticate(String username, String password) {
        UserAccount userAccount = userAccountRepository.findByUsername(username)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password"));

        if (!userAccount.isEnabled() || !passwordEncoder.matches(password, userAccount.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }

        return userAccount;
    }

    public CurrentUserResponseDto currentUser(Jwt jwt) {
        UserAccount userAccount = userAccountRepository.findByUsername(jwt.getSubject())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token"));
        return new CurrentUserResponseDto(userAccount.getId(), userAccount.getUsername(), userAccount.getRole().name(), userAccount.getDisplayName(), userAccount.isEnabled());
    }
}
