package com.aronalvarenga.rtts.identity.application;

import com.aronalvarenga.rtts.identity.domain.UserAccount;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final JwtProperties jwtProperties;

    public JwtService(JwtEncoder jwtEncoder, JwtProperties jwtProperties) {
        this.jwtEncoder = jwtEncoder;
        this.jwtProperties = jwtProperties;
    }

    public LoginResponseDto createToken(UserAccount userAccount) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(jwtProperties.getAccessTokenTtl());

        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer(jwtProperties.getIssuer())
            .issuedAt(issuedAt)
            .expiresAt(expiresAt)
            .subject(userAccount.getUsername())
            .claim("displayName", userAccount.getDisplayName())
            .claim("role", userAccount.getRole().name())
            .claim("roles", List.of(userAccount.getRole().name()))
            .claim("enabled", userAccount.isEnabled())
            .build();

        String token = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
        return new LoginResponseDto(token, expiresAt.getEpochSecond() - issuedAt.getEpochSecond(), userAccount.getUsername(), userAccount.getRole().name(), userAccount.getDisplayName());
    }

    public SecretKeySpec secretKey() {
        return new SecretKeySpec(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }
}
