package ru.stm.shcherbinki3.security.converter;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import ru.stm.shcherbinki3.dto.jwt.JwtToken;
import ru.stm.shcherbinki3.service.JwtRedisService;

import java.util.function.Function;


public class AccessJwtAuthenticationConverter implements AuthenticationConverter {

    private final Function<String, JwtToken> accessTokenStringDeserializer;

    private final Function<String, JwtToken> refreshTokenStringDeserializer;

    private final JwtRedisService jwtRedisService;

    public AccessJwtAuthenticationConverter(Function<String, JwtToken> accessTokenStringDeserializer,
                                            Function<String, JwtToken> refreshTokenStringDeserializer,
                                            JwtRedisService jwtRedisService) {
        this.accessTokenStringDeserializer = accessTokenStringDeserializer;
        this.refreshTokenStringDeserializer = refreshTokenStringDeserializer;
        this.jwtRedisService = jwtRedisService;
    }


    @Override
    public Authentication convert(HttpServletRequest request) {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new BadCredentialsException("Authorization header missing or invalid");
        }

        String token = authorization.substring(7);
        JwtToken accessToken = accessTokenStringDeserializer.apply(token);

        if (accessToken != null) {
            if (jwtRedisService.checkForAccess(accessToken)) {
                return new PreAuthenticatedAuthenticationToken(accessToken, token);
            } else {
                throw new BadCredentialsException("JWT access token not valid");
            }
        }

        JwtToken refreshToken = refreshTokenStringDeserializer.apply(token);
        if (refreshToken != null) {
            return new PreAuthenticatedAuthenticationToken(refreshToken, token);
        }

        throw new BadCredentialsException("JWT token not recognized");
    }


}
