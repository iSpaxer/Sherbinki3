package ru.stm.shcherbinki3.config;

import ru.stm.shcherbinki3.security.jwt.factory.AuthenticationJwtResponseMapper;
import ru.stm.shcherbinki3.util.ApplicationDataComponent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Order(1)
    public SecurityFilterChain chainAPI(HttpSecurity http,
                                        ApplicationDataComponent appData,
                                        JwtAuthenticationConfigurer jwtAuthenticationConfigurer) throws Exception {
        http
                .apply(jwtAuthenticationConfigurer);

        http
                .cors(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .securityMatcher("/api/**")
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/info").permitAll()
                        .requestMatchers(HttpMethod.GET, appData.glueEndpoints(
                                "/user", "/user/**", "/jwt/login", "/jwt/refresh"
                        )).permitAll()
                        .requestMatchers(appData.glueEndpoints("/user/create", "/user/restore")).anonymous()
                        .requestMatchers(appData.glueEndpoints("/user", "/user/**")).authenticated()
                )
                .sessionManagement(sessionManagement ->
                                           sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }


    @Bean
    @Order(2)
    public SecurityFilterChain chainDefault(HttpSecurity http,
                                            AuthenticationJwtResponseMapper authenticationJwtResponseMapper)
            throws Exception {
        http
                .cors(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/**").permitAll()
                )
                .sessionManagement(sessionManagement ->
                                           sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }

}
