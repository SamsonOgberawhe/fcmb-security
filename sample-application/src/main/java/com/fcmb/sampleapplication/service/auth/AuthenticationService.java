package com.fcmb.sampleapplication.service.auth;

import com.fcmb.sampleapplication.dto.request.LoginRequest;
import com.fcmb.sampleapplication.dto.response.LoginResponse;
import com.fcmb.sampleapplication.entity.User;
import com.fcmb.sampleapplication.repository.UserRepository;
import com.fcmb.security.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public LoginResponse authenticate(LoginRequest request) {
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            // Get user details
            User user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            // Generate JWT token
            List<? extends GrantedAuthority> authorities = 
                    (List<? extends GrantedAuthority>) authentication.getAuthorities();
            
            String token = jwtUtil.generateToken(user.getId(), user.getUsername(), authorities);

            log.info("User '{}' authenticated successfully", user.getUsername());

            return LoginResponse.builder()
                    .token(token)
                    .type("Bearer")
                    .userId(user.getId())
                    .username(user.getUsername())
                    .roles(user.getRoles())
                    .build();

        } catch (Exception e) {
            log.error("Authentication failed for user '{}': {}", request.getUsername(), e.getMessage());
            throw new BadCredentialsException("Invalid username or password");
        }
    }
}
