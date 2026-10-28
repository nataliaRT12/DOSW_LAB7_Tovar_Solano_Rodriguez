package edu.eci.dosw.todo.security;

import edu.eci.dosw.todo.entity.UserEntity;
import edu.eci.dosw.todo.entity.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TokenServiceTest {

    private static final String SECRET = "test-secret-for-unit-tests-only-0123456789";
    private static final String ISSUER = "todo-api";

    private final SecurityConfig config = new SecurityConfig();
    private final SecretKey key = config.jwtSigningKey(SECRET);
    private final JwtDecoder decoder = config.jwtDecoder(key, ISSUER);

    private TokenService tokenService(String issuer, long minutes) {
        return new TokenService(config.jwtEncoder(key), issuer, minutes);
    }

    private UserEntity buildUser() {
        UserEntity user = new UserEntity();
        user.setId(7L);
        user.setEmail("ana@example.com");
        user.setRole(UserRole.USER);
        return user;
    }

    @Test
    void issue_shouldCreateSignedTokenWithExpectedClaims() {
        String token = tokenService(ISSUER, 30).issue(buildUser());

        Jwt jwt = decoder.decode(token);

        assertThat(jwt.getSubject()).isEqualTo("7");
        assertThat(jwt.getClaimAsString("iss")).isEqualTo(ISSUER);
        assertThat(jwt.getClaimAsStringList(TokenService.ROLES_CLAIM)).containsExactly("USER");
        assertThat(jwt.getIssuedAt()).isNotNull();
        assertThat(Duration.between(jwt.getIssuedAt(), jwt.getExpiresAt())).isEqualTo(Duration.ofMinutes(30));
    }

    @Test
    void getExpirationSeconds_shouldReturnThirtyMinutes() {
        assertThat(tokenService(ISSUER, 30).getExpirationSeconds()).isEqualTo(1800);
    }

    @Test
    void decode_shouldRejectTamperedToken() {
        String token = tokenService(ISSUER, 30).issue(buildUser());
        String[] parts = token.split("\\.");
        char changed = parts[1].charAt(10) == 'A' ? 'B' : 'A';
        String tampered = parts[0] + "." + parts[1].substring(0, 10) + changed + parts[1].substring(11) + "." + parts[2];

        assertThatThrownBy(() -> decoder.decode(tampered)).isInstanceOf(JwtException.class);
    }

    @Test
    void decode_shouldRejectExpiredToken() {
        Instant issuedAt = Instant.now().minus(Duration.ofMinutes(40));
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .subject("7")
                .issuedAt(issuedAt)
                .expiresAt(issuedAt.plus(Duration.ofMinutes(30)))
                .build();
        String token = config.jwtEncoder(key)
                .encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();

        assertThatThrownBy(() -> decoder.decode(token)).isInstanceOf(JwtException.class);
    }

    @Test
    void decode_shouldRejectTokenFromAnotherIssuer() {
        String token = tokenService("otro-emisor", 30).issue(buildUser());

        assertThatThrownBy(() -> decoder.decode(token)).isInstanceOf(JwtException.class);
    }

    @Test
    void decode_shouldRejectTokenSignedWithAnotherKey() {
        SecretKey otherKey = config.jwtSigningKey("another-secret-for-unit-tests-0123456789");
        String token = new TokenService(config.jwtEncoder(otherKey), ISSUER, 30).issue(buildUser());

        assertThatThrownBy(() -> decoder.decode(token)).isInstanceOf(JwtException.class);
    }

    @Test
    void jwtSigningKey_shouldRejectShortSecret() {
        assertThatThrownBy(() -> config.jwtSigningKey("corta"))
                .isInstanceOf(IllegalStateException.class);
    }
}
