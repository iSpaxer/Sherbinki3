package ru.stm.shcherbinki3.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;
import ru.stm.shcherbinki3.security.converter.AccessJwtAuthenticationConverter;
import ru.stm.shcherbinki3.util.exception.ErrorResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class JwtAuthenticationFilter extends AuthenticationFilter {
    private final RequestMatcher publicEndpoints;

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager, AccessJwtAuthenticationConverter converter,
                                   RequestMatcher publicEndpoints) {
        super(authenticationManager, converter);
        this.publicEndpoints = publicEndpoints;
        this.setSuccessHandler((request, response, authentication) -> {
            SecurityContextHolder.getContext().setAuthentication(authentication);
        });
        this.setFailureHandler((request, response, e) -> {
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.getWriter().write(new ObjectMapper().writeValueAsString(new ErrorResponse(
                    HttpStatus.UNAUTHORIZED.value(),
                    HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                    e.getMessage() != null ? e.getMessage() : "Invalid request",
                    request.getRequestURI())));
        });
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (publicEndpoints.matches(request)) {
            chain.doFilter(request, response);
            return;
        }
        super.doFilterInternal(request, response, chain);
    }
}
