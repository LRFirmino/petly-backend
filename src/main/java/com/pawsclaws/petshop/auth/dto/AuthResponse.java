package com.pawsclaws.petshop.auth.dto;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        UserSummary user) {

    public static AuthResponse of(String token, long expiresIn, UserSummary user) {
        return new AuthResponse(token, "Bearer", expiresIn, user);
    }
}
