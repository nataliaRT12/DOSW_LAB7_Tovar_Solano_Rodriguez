package edu.eci.dosw.todo.security;

import edu.eci.dosw.todo.entity.UserEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class TokenService {

    public static final String ROLES_CLAIM = "roles";

    private final JwtEncoder encoder;
    private final String issuer;
    private final Duration expiration;

    public TokenService(JwtEncoder encoder,
                        @Value("${jwt.issuer}") String issuer,
                        @Value("${jwt.expiration-minutes}") long expirationMinutes) {
        this.encoder = encoder;
        this.issuer = issuer;
        this.expiration = Duration.ofMinutes(expirationMinutes);
    }

    public String issue(UserEntity user) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(now.plus(expiration))
                .subject(String.valueOf(user.getId()))
                .claim("email", user.getEmail())
                .claim(ROLES_CLAIM, List.of(user.getRole().name()))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    public long getExpirationSeconds() {
        return expiration.toSeconds();
    }
}
