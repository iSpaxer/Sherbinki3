package ru.stm.shcherbinki3.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationFilter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.servlet.HandlerExceptionResolver;
import ru.stm.shcherbinki3.dto.jwt.JwtToken;
import ru.stm.shcherbinki3.security.JwtAuthenticationUserDetailsService;
import ru.stm.shcherbinki3.security.JwtUserDetailsService;
import ru.stm.shcherbinki3.security.converter.AccessJwtAuthenticationConverter;
import ru.stm.shcherbinki3.security.filter.*;
import ru.stm.shcherbinki3.security.jwt.factory.AuthenticationJwtResponseMapper;
import ru.stm.shcherbinki3.service.JwtRedisService;
import ru.stm.shcherbinki3.util.ApplicationDataComponent;
import ru.stm.shcherbinki3.util.exception.entrypoint.ForbiddenEntryPoint;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Конфигурация JWT фильтров для Spring Security
 */
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


    private final RequestMatcher publicEndpoints;

    public JwtAuthenticationConfigurer(JwtUserDetailsService userDetailsService, PasswordEncoder passwordEncoder,
                                       Function<Authentication, JwtToken> jwtRefreshFactory,
                                       Function<JwtToken, JwtToken> jwtAccessFactory,
                                       Function<JwtToken, String> accessTokenSerializer,
                                       Function<JwtToken, String> refreshTokenSerializer,
                                       Function<String, JwtToken> accessTokenDeserializer,
                                       Function<String, JwtToken> refreshTokenDeserializer,
                                       HandlerExceptionResolver handlerExceptionResolver, ObjectMapper objectMapper,
                                       ApplicationDataComponent dataComponent,
                                       AuthenticationJwtResponseMapper authenticationJwtResponseMapper,
                                       JwtRedisService jwtRedisService, ForbiddenEntryPoint forbiddenEntryPoint,
                                       ApplicationDataComponent addData) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.jwtRefreshFactory = jwtRefreshFactory;
        this.jwtAccessFactory = jwtAccessFactory;
        this.accessTokenSerializer = accessTokenSerializer;
        this.refreshTokenSerializer = refreshTokenSerializer;
        this.accessTokenDeserializer = accessTokenDeserializer;
        this.refreshTokenDeserializer = refreshTokenDeserializer;
        this.handlerExceptionResolver = handlerExceptionResolver;
        this.objectMapper = objectMapper;
        this.dataComponent = dataComponent;
        this.authenticationJwtResponseMapper = authenticationJwtResponseMapper;
        this.jwtRedisService = jwtRedisService;
        this.forbiddenEntryPoint = forbiddenEntryPoint;

        this.publicEndpoints =new OrRequestMatcher(
                Stream.concat(
                        Arrays.stream(addData.glueEndpoints(SecurityConfig.PUBLIC_API_VERSION))
                                .map(AntPathRequestMatcher::new),
                        Arrays.stream(SecurityConfig.PUBLIC_API)
                                .map(AntPathRequestMatcher::new) // Добавляем /api/info
                ).toArray(RequestMatcher[]::new));
    }

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

        var jwtAuthenticationFilter = new JwtAuthenticationFilter(
                http.getSharedObject(AuthenticationManager.class),
                new AccessJwtAuthenticationConverter(accessTokenDeserializer, refreshTokenDeserializer, jwtRedisService),
                publicEndpoints
        );



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
