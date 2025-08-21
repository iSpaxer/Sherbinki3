package ru.stm.shcherbinki3.security;

import  ru.stm.shcherbinki3.model.User;
import ru.stm.shcherbinki3.security.auth.AuthPrincipalAbstractIdentifier;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class JwtUserDetails extends User implements UserDetails, AuthPrincipalAbstractIdentifier {

    private final String jti;

    public JwtUserDetails(Long id, String email, @NotNull String password, String jti) {
        super(id, email, password);
        this.jti = jti;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getUsername() {
        return getEmail();
    }


    @Override
    public String getJti() {
        return jti;
    }
}
