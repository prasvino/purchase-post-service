package com.app.auth.dto;

import com.app.user.dto.UserSummary;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private String token;
    private UserSummary user;
}