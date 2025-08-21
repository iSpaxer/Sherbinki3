package ru.stm.shcherbinki3.security.jwt.factory;

import ru.stm.shcherbinki3.dto.jwt.JwtToken;

import java.util.function.Function;

public interface JwtAccessTokenFactory extends Function<JwtToken, JwtToken> {
}
