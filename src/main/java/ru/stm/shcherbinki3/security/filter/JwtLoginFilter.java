package ru.stm.shcherbinki3.security.filter;

import ru.stm.shcherbinki3.dto.jwt.JwtResponse;
import ru.stm.shcherbinki3.security.converter.CustomAuthenticationConverter;
import ru.stm.shcherbinki3.util.ApplicationDataComponent;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.function.Function;


/**
 * Фильтр для аутификации пользователя по {email: ..; password: ..;}
 * Фильтр не пропускает запрос в сервлет, отдает ответ сам.
 */
public class JwtLoginFilter extends OncePerRequestFilter {

    private final ApplicationDataComponent dataComponent;
    private final DaoAuthenticationProvider daoAuthenticationProvider;
    private final Function<Authentication, JwtResponse> authenticationJwtResponseMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final RequestMatcher requestMatcher;

    public JwtLoginFilter(ApplicationDataComponent dataComponent, DaoAuthenticationProvider daoAuthenticationProvider,
                          Function<Authentication, JwtResponse> authenticationJwtResponseMapper) {
        this.dataComponent = dataComponent;
        this.daoAuthenticationProvider = daoAuthenticationProvider;
        this.authenticationJwtResponseMapper = authenticationJwtResponseMapper;
        this.requestMatcher = new AntPathRequestMatcher(dataComponent.glueEndpoint("/jwt/login"),
                                                        HttpMethod.POST.name());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {
        if (this.requestMatcher.matches(request)) {
            var authentication = new CustomAuthenticationConverter().convert(request);
            Authentication authenticate;
            authenticate = daoAuthenticationProvider.authenticate(authentication);
            response.setStatus(HttpStatus.OK.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(response.getWriter(), authenticationJwtResponseMapper.apply(authenticate));
            return;
        }
        filterChain.doFilter(request, response);
    }
}
