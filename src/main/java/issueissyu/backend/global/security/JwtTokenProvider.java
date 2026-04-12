package issueissyu.backend.global.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import issueissyu.backend.global.config.properties.JwtProperties;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    public static final String CLAIM_TOKEN_TYPE = "typ";
    public static final String TOKEN_TYPE_ACCESS = "ACCESS";
    public static final String TOKEN_TYPE_REFRESH = "REFRESH";

    private final SecretKey key;
    private final long accessExpMs;
    private final long refreshExpMs;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.accessExpMs = jwtProperties.getAccessExpMs();
        this.refreshExpMs = jwtProperties.getRefreshExpMs();
    }

    public String createAccessToken(String uid) {
        return buildToken(uid, TOKEN_TYPE_ACCESS, accessExpMs);
    }

    public String createRefreshToken(String uid) {
        return buildToken(uid, TOKEN_TYPE_REFRESH, refreshExpMs);
    }

    private String buildToken(String uid, String typ, long ttlMs) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + ttlMs);
        return Jwts.builder()
                .subject(uid)
                .issuedAt(now)
                .expiration(exp)
                .claim(CLAIM_TOKEN_TYPE, typ)
                .signWith(key)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException | MalformedJwtException | UnsupportedJwtException
                 | IllegalArgumentException e) {
            return false;
        }
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String parseUid(String token) {
        return parseClaims(token).getSubject();
    }

    public String parseTokenType(String token) {
        Claims claims = parseClaims(token);
        Object typ = claims.get(CLAIM_TOKEN_TYPE);
        return typ != null ? typ.toString() : null;
    }

    public long getAccessExpMs() {
        return accessExpMs;
    }

    public long getRefreshExpMs() {
        return refreshExpMs;
    }
}
