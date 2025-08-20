package ru.stm.shcherbinki3.security.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.SneakyThrows;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationConverter;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;

public class CustomAuthenticationConverter implements AuthenticationConverter {

    @SneakyThrows
    @Override
    public Authentication convert(HttpServletRequest request) {

        StringBuilder jsonStringBuilder = new StringBuilder();
        String line;
        try (BufferedReader reader = request.getReader()) {
            while ((line = reader.readLine()) != null) {
                jsonStringBuilder.append(line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Преобразование JSON-строки в Java объект
        var objectMapper = new ObjectMapper();
        Map<String, String> jsonMap = objectMapper.readValue(jsonStringBuilder.toString(), Map.class);

        // Получение значений из Map
        var email = jsonMap.get("username");
        var password = jsonMap.get("password");

        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
            throw new BadCredentialsException("Пароль или логин пусты.");
        }
        return new UsernamePasswordAuthenticationToken(email, password);
    }
}
