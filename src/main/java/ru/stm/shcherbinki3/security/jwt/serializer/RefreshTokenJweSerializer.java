package ru.stm.shcherbinki3.security.jwt.serializer;


import ru.stm.shcherbinki3.dto.jwt.JwtToken;
import com.nimbusds.jose.*;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWTClaimsSet;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.sql.Date;
import java.util.function.Function;

@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RefreshTokenJweSerializer implements Function<JwtToken, String> {

    JWEEncrypter jweEncrypter;
    JWEAlgorithm jweAlgorithm;
    EncryptionMethod encryptionMethod;

    public RefreshTokenJweSerializer(JWEEncrypter jweEncrypter) {
        this.jweEncrypter = jweEncrypter;
        jweAlgorithm = JWEAlgorithm.DIR;
        encryptionMethod = EncryptionMethod.A128GCM;
    }


    @Override
    public String apply(JwtToken token) {
        var jweHeader = new JWEHeader.Builder(jweAlgorithm, encryptionMethod)
                .build();
        var jwsClaims = new JWTClaimsSet.Builder()
                .jwtID(token.jti())
                .issueTime(Date.from(token.createdAt()))
                .expirationTime(Date.from(token.expiresAt()))
                .claim("authorities", token.authorities())
                .claim("id", token.id())
                .build();
        var encryptedJWT = new EncryptedJWT(jweHeader, jwsClaims);
        try {
            encryptedJWT.encrypt(jweEncrypter);
            return encryptedJWT.serialize();
        } catch (JOSEException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
    }


}
