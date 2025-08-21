package ru.stm.shcherbinki3.dto.jwt;

public record JwtResponse(String accessToken, String expiryAccessToken,
                          String refreshToken, String expiryRefreshToken) {
}
