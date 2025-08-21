package ru.stm.shcherbinki3.security.jwt.deserializer;

import org.springframework.security.authentication.BadCredentialsException;
import ru.stm.shcherbinki3.dto.jwt.JwtToken;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.util.function.Function;


@Slf4j
@RequiredArgsConstructor
public class AccessTokenJwsDeserializer implements Function<String, JwtToken> {

    private final JWSVerifier jwsVerifier;
    private final JWSAlgorithm jwsAlgorithm;

    public AccessTokenJwsDeserializer(JWSVerifier jwsVerifier) {
        this.jwsVerifier = jwsVerifier;
        this.jwsAlgorithm = JWSAlgorithm.HS256;
    }

    @Override
    public JwtToken apply(String stringToken) {
        try {
            var signedJWT = SignedJWT.parse(stringToken);
            if (signedJWT.verify(jwsVerifier)) {
                var jwtClaimsSet = signedJWT.getJWTClaimsSet();
                return new JwtToken(
                        jwtClaimsSet.getLongClaim("id"),
                        jwtClaimsSet.getJWTID(),
                        jwtClaimsSet.getStringListClaim("authorities"),
                        jwtClaimsSet.getIssueTime().toInstant(),
                        jwtClaimsSet.getExpirationTime().toInstant()
                );
            }
        } catch (ParseException | JOSEException e) {
            throw new BadCredentialsException(e.getMessage());
        }
        return null;
    }


}
