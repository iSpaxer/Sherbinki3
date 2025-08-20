package ru.stm.shcherbinki3.config;

import org.springframework.http.HttpStatus;
import ru.stm.shcherbinki3.dto.jwt.JwtToken;
import ru.stm.shcherbinki3.security.JwtAuthenticationUserDetailsService;
import ru.stm.shcherbinki3.security.JwtUserDetailsService;
import ru.stm.shcherbinki3.security.converter.AccessJwtAuthenticationConverter;
import ru.stm.shcherbinki3.security.filter.JwtExceptionHandlerFilter;
import ru.stm.shcherbinki3.security.filter.JwtLoginFilter;
import ru.stm.shcherbinki3.security.filter.JwtLogoutFilter;
import ru.stm.shcherbinki3.security.filter.JwtRefreshFilter;
import ru.stm.shcherbinki3.security.jwt.factory.AuthenticationJwtResponseMapper;
import ru.stm.shcherbinki3.service.JwtRedisService;
import ru.stm.shcherbinki3.util.ApplicationDataComponent;
import ru.stm.shcherbinki3.util.exception.ErrorResponse;
import ru.stm.shcherbinki3.util.exception.entrypoint.ForbiddenEntryPoint;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationFilter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.nio.charset.StandardCharsets;
import java.util.function.Function;

/**
 * Конфигурация JWT фильтров для Spring Security
 */
@RequiredArgsConstructor
public class JwtAuthenticationConfigurer extends AbstractHttpConfigurer<JwtAuthenticationConfigurer, HttpSecurity> {
    private final JwtUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    private final Function<Authentication, JwtToken> jwtRefreshFactory;
    private final Function<JwtToken, JwtToken> jwtAccessFactory;

    private final Function<JwtToken, String> accessTokenSerializer;
    private final Function<JwtToken, String> refreshTokenSerializer;


    private final Function<String, JwtToken> accessTokenDeserializer;
    private final Function<String, JwtToken> refreshTokenDeserializer;

    private final HandlerExceptionResolver handlerExceptionResolver;
    private final ObjectMapper objectMapper;
    private final ApplicationDataComponent dataComponent;
    private final AuthenticationJwtResponseMapper authenticationJwtResponseMapper;

    private final JwtRedisService jwtRedisService;
    private final ForbiddenEntryPoint forbiddenEntryPoint;

    @Override
    public void configure(HttpSecurity http) throws Exception {
        var daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(userDetailsService);
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder);

        var jwtLoginFilter = new JwtLoginFilter(
                dataComponent,
                daoAuthenticationProvider,
                authenticationJwtResponseMapper
        );

        var jwtRefreshFilter = new JwtRefreshFilter(
                dataComponent,
                refreshTokenDeserializer,
                jwtAccessFactory,
                accessTokenSerializer,
                refreshTokenSerializer,
                jwtRedisService);

        var jwtAuthenticationFilter = new AuthenticationFilter(
                http.getSharedObject(AuthenticationManager.class),
                new AccessJwtAuthenticationConverter(accessTokenDeserializer, refreshTokenDeserializer, jwtRedisService)
        );

        jwtAuthenticationFilter
                .setFailureHandler((request, response, e) -> {
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                    response.getWriter().write(objectMapper.writeValueAsString(new ErrorResponse(
                            HttpStatus.BAD_REQUEST.value(),
                            HttpStatus.BAD_REQUEST.getReasonPhrase(),
                            e.getMessage() != null ? e.getMessage() : "Invalid request",
                            request.getRequestURI())));
                });
        jwtAuthenticationFilter
                .setSuccessHandler((request, response, authentication) -> {
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                });


        var authenticationProvider = new PreAuthenticatedAuthenticationProvider();
        authenticationProvider.setPreAuthenticatedUserDetailsService(
                new JwtAuthenticationUserDetailsService());

        var jwtLogoutFilter = new JwtLogoutFilter(dataComponent, jwtRedisService);

        http
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(forbiddenEntryPoint)
                )
                .addFilterAfter(jwtLoginFilter, BasicAuthenticationFilter.class)
                .addFilterAfter(jwtRefreshFilter, JwtLoginFilter.class)
                .addFilterBefore(new JwtExceptionHandlerFilter(handlerExceptionResolver, objectMapper),
                                 JwtLoginFilter.class)
                .addFilterAfter(jwtAuthenticationFilter, CsrfFilter.class)
                .addFilterAfter(jwtLogoutFilter, AuthenticationFilter.class)
                .authenticationProvider(authenticationProvider)
                .authenticationProvider(daoAuthenticationProvider);

    }

}
