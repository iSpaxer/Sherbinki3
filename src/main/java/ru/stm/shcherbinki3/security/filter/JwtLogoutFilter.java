package ru.stm.shcherbinki3.security.filter;

import ru.stm.shcherbinki3.security.DefaultAuthenticationPrincipal;
import ru.stm.shcherbinki3.service.JwtRedisService;
import ru.stm.shcherbinki3.util.ApplicationDataComponent;
import ru.stm.shcherbinki3.util.exception.BadRequestException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtLogoutFilter extends OncePerRequestFilter {

    private final ApplicationDataComponent dataComponent;
    private final JwtRedisService jwtRedisService;
    private final RequestMatcher requestMatcher;

    public JwtLogoutFilter(ApplicationDataComponent dataComponent, JwtRedisService jwtRedisService) {
        this.dataComponent = dataComponent;
        this.jwtRedisService = jwtRedisService;
        this.requestMatcher = new AntPathRequestMatcher(this.dataComponent.glueEndpoint("/jwt/logout"),
                                                        HttpMethod.POST.name());

    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (this.requestMatcher.matches(request)) {
            SecurityContext context = SecurityContextHolder.getContext();
            if (context != null && context.getAuthentication() != null) {
                Authentication authentication = context.getAuthentication();
                if (authentication.getPrincipal() instanceof DefaultAuthenticationPrincipal defaultAuthenticationPrincipal) {
                    jwtRedisService.logout(defaultAuthenticationPrincipal.getToken());
                    response.setStatus(HttpStatus.OK.value());
                    return;
                }
            }
            throw new BadRequestException("Error logout repeat again");
        }
        filterChain.doFilter(request, response);
    }
}
