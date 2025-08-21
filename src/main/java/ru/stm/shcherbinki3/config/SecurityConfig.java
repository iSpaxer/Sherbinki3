package ru.stm.shcherbinki3.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Configuration
public class SecurityConfig {


    public final static List<String> PUBLIC_API_VERSION =
            Arrays.asList("/jwt/login", "/jwt/refresh",
                          "/user/create", "/user/restore",
                          "/routes", "/route/*/tickets");

    public final static String[] PUBLIC_API = {"/api/info"};

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
                        .requestMatchers(PUBLIC_API).permitAll()
                        .requestMatchers(appData.glueEndpoints(PUBLIC_API_VERSION)).permitAll()
                        .anyRequest().authenticated()
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
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**")
                        .permitAll()
                        .requestMatchers("/**")
                        .permitAll()
                )
                .sessionManagement(sessionManagement ->
                                           sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }

}
