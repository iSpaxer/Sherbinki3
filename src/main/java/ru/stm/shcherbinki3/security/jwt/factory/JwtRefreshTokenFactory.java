package ru.stm.shcherbinki3.security.jwt.factory;

import ru.stm.shcherbinki3.dto.jwt.JwtToken;
import org.springframework.security.core.Authentication;

import java.util.function.Function;

public interface JwtRefreshTokenFactory extends Function<Authentication, JwtToken> {
}
