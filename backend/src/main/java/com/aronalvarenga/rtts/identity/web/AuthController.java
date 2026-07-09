package com.aronalvarenga.rtts.identity.web;

import com.aronalvarenga.rtts.identity.application.AuthService;
import com.aronalvarenga.rtts.identity.application.CurrentUserResponseDto;
import com.aronalvarenga.rtts.identity.application.JwtService;
import com.aronalvarenga.rtts.identity.application.LoginResponseDto;
import com.aronalvarenga.rtts.modules.common.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    public AuthController(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public LoginResponseDto login(@Valid @RequestBody LoginRequest request) {
        return jwtService.createToken(authService.authenticate(request.username(), request.password()));
    }

    @PostMapping("/logout")
    public ApiResponse logout() {
        return ApiResponse.success("Logged out");
    }

    @GetMapping("/me")
    public CurrentUserResponseDto me(@AuthenticationPrincipal Jwt jwt) {
        return authService.currentUser(jwt);
    }
}
