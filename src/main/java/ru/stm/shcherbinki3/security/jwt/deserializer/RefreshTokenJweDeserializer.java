package ru.stm.shcherbinki3.security.jwt.deserializer;

import org.springframework.security.authentication.BadCredentialsException;
import ru.stm.shcherbinki3.dto.jwt.JwtToken;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEDecrypter;
import com.nimbusds.jose.shaded.gson.JsonParseException;
import com.nimbusds.jwt.EncryptedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.util.function.Function;

@Slf4j
@RequiredArgsConstructor
public class RefreshTokenJweDeserializer implements Function<String, JwtToken> {

    private final JWEDecrypter jweDecrypter;

    @Override
    public JwtToken apply(String string) {
        try {
            var encryptedJWT = EncryptedJWT.parse(string);
            encryptedJWT.decrypt(this.jweDecrypter);
            var claimsSet = encryptedJWT.getJWTClaimsSet();
            return new JwtToken(
                    claimsSet.getLongClaim("id"),
                    claimsSet.getJWTID(),
                    claimsSet.getStringListClaim("authorities"),
                    claimsSet.getIssueTime().toInstant(),
                    claimsSet.getExpirationTime().toInstant());
        } catch (ParseException | JOSEException exception) {
            throw new BadCredentialsException("Refresh токен не десериализуем");
        }
    }
}
