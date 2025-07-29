package org.liudmylamalomuzh.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.liudmylamalomuzh.dto.LoginUserDto;
import org.liudmylamalomuzh.dto.RegisterUserDto;
import org.liudmylamalomuzh.dto.UserIdResponseDto;
import org.liudmylamalomuzh.entity.User;
import org.liudmylamalomuzh.repository.UserRepository;
import org.liudmylamalomuzh.service.AuthenticationService;
import org.liudmylamalomuzh.service.JwtService;
import org.liudmylamalomuzh.service.TokenBlacklistService;
import org.liudmylamalomuzh.utils.LoginResponse;
import org.springframework.expression.AccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final JwtService jwtService;

    private final AuthenticationService authenticationService;
    private UserRepository userRepository;
    private final TokenBlacklistService tokenBlacklistService;

    public AuthController(JwtService jwtService, AuthenticationService authenticationService, TokenBlacklistService tokenBlacklistService) {
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @PostMapping("/signup")
    public ResponseEntity<UserIdResponseDto> register(@RequestBody RegisterUserDto registerUserDto) throws AccessException {
        User registeredUser = authenticationService.signup(registerUserDto);
        return ResponseEntity.ok(new UserIdResponseDto(registeredUser.getId()));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticate(@RequestBody LoginUserDto loginUserDto) {
        User authenticatedUser = authenticationService.authenticate(loginUserDto);

        String jwtToken = jwtService.generateToken(authenticatedUser);

        LoginResponse loginResponse = new LoginResponse()
                .setToken(jwtToken)
                .setExpiresIn(jwtService.getExpirationTime());

        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        // Extract token from the Authorization header if present
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            // Blacklist the token so it can no longer be used
            tokenBlacklistService.blacklistToken(token);
        }
        return ResponseEntity.ok().build();
    }
}
