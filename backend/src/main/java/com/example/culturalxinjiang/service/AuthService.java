package com.example.culturalxinjiang.service;

import com.example.culturalxinjiang.dto.request.LoginRequest;
import com.example.culturalxinjiang.dto.request.RegisterRequest;
import com.example.culturalxinjiang.dto.response.LoginResponse;
import com.example.culturalxinjiang.dto.response.UserInfoResponse;
import com.example.culturalxinjiang.entity.User;
import com.example.culturalxinjiang.repository.UserRepository;
import com.example.culturalxinjiang.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public LoginResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("邮箱已被注册");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(User.Role.USER)
                .enabled(true)
                .build();

        user = userRepository.save(user);

        String token = tokenProvider.generateToken(user.getUsername());

        return LoginResponse.builder()
                .token(token)
                .userInfo(mapToUserInfoResponse(user))
                .build();
    }

    public LoginResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            User user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new RuntimeException("用户不存在"));

            String token = tokenProvider.generateToken(user.getUsername());

            return LoginResponse.builder()
                    .token(token)
                    .userInfo(mapToUserInfoResponse(user))
                    .build();
        } catch (BadCredentialsException e) {
            throw new RuntimeException("用户名或密码错误");
        } catch (AuthenticationException e) {
            throw new RuntimeException("认证失败: " + e.getMessage());
        }
    }

    private UserInfoResponse mapToUserInfoResponse(User user) {
        return UserInfoResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .nickname(user.getNickname())
                .bio(user.getBio())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}



