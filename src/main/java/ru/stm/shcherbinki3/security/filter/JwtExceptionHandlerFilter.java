package ru.stm.shcherbinki3.security.filter;

import org.springframework.http.HttpStatus;
import ru.stm.shcherbinki3.util.exception.BadRequestException;
import ru.stm.shcherbinki3.util.exception.ErrorResponse;
import ru.stm.shcherbinki3.util.exception.ForbiddenException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.shaded.gson.JsonParseException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;
import ru.stm.shcherbinki3.util.exception.ResourceNotFoundException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;


@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JwtExceptionHandlerFilter extends OncePerRequestFilter {

    HandlerExceptionResolver handlerExceptionResolver;
    ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            if (e.getCause() instanceof ResourceNotFoundException notFoundException) {
                log.error(notFoundException.getMessage());
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter()
                        .write(objectMapper.writeValueAsString(new ErrorResponse(
                                HttpStatus.NOT_FOUND.value(),
                                HttpStatus.NOT_FOUND.getReasonPhrase(),
                                e.getMessage(),
                                request.getRequestURI()
                        )));
                return;
            }

            if (e instanceof JsonParseException || e instanceof BadRequestException) {
                log.error(e.getMessage());
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter().write(objectMapper.writeValueAsString(new ErrorResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        HttpStatus.BAD_REQUEST.getReasonPhrase(),
                        e.getMessage(),
                        request.getRequestURI()
                )));
                return;
            }

            if (e instanceof ForbiddenException forbiddenException) {
                log.error(forbiddenException.getMessage());
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter()
                        .write(objectMapper.writeValueAsString(new ErrorResponse(
                                HttpStatus.FORBIDDEN.value(),
                                HttpStatus.FORBIDDEN.getReasonPhrase(),
                                e.getMessage(),
                                request.getRequestURI()
                        )));
                return;
            }


            log.error("Custom error Spring Security Filter Chain Exception:", e);
            if (handlerExceptionResolver.resolveException(request, response, null, e) == null) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("Inner error in filter chain. " + e.getMessage());
            }
        }
    }
}
