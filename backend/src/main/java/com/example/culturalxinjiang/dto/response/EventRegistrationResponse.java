package com.example.culturalxinjiang.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventRegistrationResponse {
    private Long id;
    private Long userId;
    private String username;
    private String nickname;
    private String email;
    private String phone;
    private LocalDateTime registeredAt;
    private String status;
    private String remark;
    private LocalDateTime processedAt;
    private String processedBy;
    private String processedByNickname;
}



