package ru.stm.shcherbinki3.security;

import ru.stm.shcherbinki3.dto.jwt.JwtToken;
import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.time.Instant;


@Getter
public class DefaultAuthenticationPrincipal extends User {

    private final JwtToken token;


    public DefaultAuthenticationPrincipal(JwtToken token) {
        super(token.id().toString(), "", true, true, token.expiresAt().isAfter(Instant.now()), true,
              token.authorities().stream()
                      .map(SimpleGrantedAuthority::new)
                      .toList());
        this.token = token;
    }

    public Long getId() {
        return token.id();
    }

}