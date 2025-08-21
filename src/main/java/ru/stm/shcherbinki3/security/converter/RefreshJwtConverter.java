package ru.stm.shcherbinki3.security.converter;


import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.SneakyThrows;
import org.springframework.security.authentication.BadCredentialsException;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;
import java.util.function.Function;

public class RefreshJwtConverter implements Function<HttpServletRequest, String> {

    @Override
    @SneakyThrows
    public String apply(HttpServletRequest request) {
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
        var refresh = jsonMap.get("refresh");

        if (refresh == null || refresh.isEmpty()) {
            throw new BadCredentialsException("Пароль или логин пусты.");
        }
        return refresh;
    }

}
