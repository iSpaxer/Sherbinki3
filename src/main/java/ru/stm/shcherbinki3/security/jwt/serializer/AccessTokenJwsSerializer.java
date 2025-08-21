package ru.stm.shcherbinki3.security.jwt.serializer;

import ru.stm.shcherbinki3.dto.jwt.JwtToken;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.sql.Date;
import java.util.function.Function;

@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AccessTokenJwsSerializer implements Function<JwtToken, String> {

    JWSSigner jwsSigner;
    JWSAlgorithm algorithm;

    public AccessTokenJwsSerializer(JWSSigner jwsSigner) {
        this.jwsSigner = jwsSigner;
        this.algorithm = JWSAlgorithm.HS256;
    }

    @Override
    public String apply(JwtToken token) {
        var jwsHeader = new JWSHeader.Builder(algorithm)
                .build();
        var jwsClaims = new JWTClaimsSet.Builder()
                .jwtID(token.jti())
                .issueTime(Date.from(token.createdAt()))
                .expirationTime(Date.from(token.expiresAt()))
                .claim("authorities", token.authorities())
                .claim("id", token.id())
                .build();
        var signedJWT = new SignedJWT(jwsHeader, jwsClaims);
        try {
            signedJWT.sign(jwsSigner);
            return signedJWT.serialize();
        } catch (JOSEException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
    }

}
