package ru.stm.shcherbinki3.config;

import ru.stm.shcherbinki3.security.JwtUserDetailsService;
import ru.stm.shcherbinki3.security.jwt.deserializer.AccessTokenJwsDeserializer;
import ru.stm.shcherbinki3.security.jwt.deserializer.RefreshTokenJweDeserializer;
import ru.stm.shcherbinki3.security.jwt.factory.*;
import ru.stm.shcherbinki3.security.jwt.serializer.AccessTokenJwsSerializer;
import ru.stm.shcherbinki3.security.jwt.serializer.RefreshTokenJweSerializer;
import ru.stm.shcherbinki3.service.JwtRedisService;
import ru.stm.shcherbinki3.util.ApplicationDataComponent;
import ru.stm.shcherbinki3.util.exception.entrypoint.ForbiddenEntryPoint;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.crypto.DirectDecrypter;
import com.nimbusds.jose.crypto.DirectEncrypter;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.text.ParseException;

@Configuration
public class JwtCommonConfig {

    private final JwtRefreshTokenFactory jwtRefreshTokenFactory;
    private final JwtAccessTokenFactory jwtAccessTokenFactory;
    private final AccessTokenJwsSerializer accessTokenJwsSerializer;
    private final RefreshTokenJweSerializer refreshTokenJweSerializer;
    private final AccessTokenJwsDeserializer accessTokenJwsDeserializer;
    private final RefreshTokenJweDeserializer refreshTokenJweDeserializer;

    public JwtCommonConfig(
            @Value("${jwt.access-token-key}") String accessTokenKey,
            @Value("${jwt.refresh-token-key}") String refreshTokenKey) throws ParseException, JOSEException {
        this.jwtRefreshTokenFactory = new DefaultJwtRefreshTokenFactory();
        this.jwtAccessTokenFactory = new DefaultJwtAccessTokenFactory();
        this.accessTokenJwsSerializer = new AccessTokenJwsSerializer(
                new MACSigner(OctetSequenceKey.parse(accessTokenKey)));
        this.refreshTokenJweSerializer = new RefreshTokenJweSerializer(
                new DirectEncrypter(OctetSequenceKey.parse(refreshTokenKey)));
        this.accessTokenJwsDeserializer = new AccessTokenJwsDeserializer(
                new MACVerifier(OctetSequenceKey.parse(accessTokenKey)));
        this.refreshTokenJweDeserializer = new RefreshTokenJweDeserializer(
                new DirectDecrypter(OctetSequenceKey.parse(refreshTokenKey)));
    }

    @Bean
    public AuthenticationJwtResponseMapper authenticationJwtResponseMapper(JwtRedisService jwtRedisService) {
        return AuthenticationJwtResponseMapper.builder()
                .jwtRefreshFactory(jwtRefreshTokenFactory)
                .jwtAccessFactory(jwtAccessTokenFactory)
                .accessTokenSerializer(accessTokenJwsSerializer)
                .refreshTokenSerializer(refreshTokenJweSerializer)
                .jwtRedisService(jwtRedisService)
                .build();
    }


    @Bean
    public JwtAuthenticationConfigurer jwtAuthenticationConfigurer(
            JwtUserDetailsService jwtUserDetailsService,
            PasswordEncoder passwordEncoder,
            HandlerExceptionResolver handlerExceptionResolver,
            ObjectMapper objectMapper,
            ApplicationDataComponent applicationDataComponent,
            AuthenticationJwtResponseMapper authenticationJwtResponseMapper,
            JwtRedisService jwtRedisService,
            ForbiddenEntryPoint forbiddenEntryPoint) {
        return new JwtAuthenticationConfigurer(
                jwtUserDetailsService,
                passwordEncoder,
                jwtRefreshTokenFactory,
                jwtAccessTokenFactory,
                accessTokenJwsSerializer,
                refreshTokenJweSerializer,
                accessTokenJwsDeserializer,
                refreshTokenJweDeserializer,
                handlerExceptionResolver,
                objectMapper,
                applicationDataComponent,
                authenticationJwtResponseMapper,
                jwtRedisService,
                forbiddenEntryPoint
        );
    }


}
