package pierro.lmsPlatform.Security.Jwt;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pierro.lmsPlatform.Entity.Auth.User;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class JwtService {
    public enum TokenType { ACCESS, REFRESH }

    /** Thông tin cần thiết sau khi token đã được xác thực chữ ký / hạn / loại. */
    public record TokenInfo(Long userId, int version) {}

    private static final String ISSUER = "lms-platform";
    private static final String CLAIM_TYPE = "type";
    private static final String CLAIM_VERSION = "ver";

    private final byte[] secret;
    private final Duration accessTtl;
    private final Duration refreshTtl;

    public JwtService(@Value("${APP_SIGNER_KEY}") String signerKey,
                      @Value("${app.jwt.expiration-minutes:60}") long accessMinutes,
                      @Value("${app.jwt.refresh-expiration-days:7}") long refreshDays) {
        byte[] bytes = signerKey == null ? new byte[0] : signerKey.getBytes(StandardCharsets.UTF_8);

        if (bytes.length < 64) {
            throw new IllegalStateException("APP_SIGNER_KEY phải dài tối thiểu 64 byte cho HS512");
        }
        this.secret = bytes;
        this.accessTtl = Duration.ofMinutes(accessMinutes);
        this.refreshTtl = Duration.ofDays(refreshDays);
    }

    public String createAccessToken(User user) {
        return create(user, TokenType.ACCESS, accessTtl);
    }

    public String createRefreshToken(User user) {
        return create(user, TokenType.REFRESH, refreshTtl);
    }

    private String create(User user, TokenType type, Duration ttl) {
        Instant now = Instant.now();
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .issuer(ISSUER)
                .subject(String.valueOf(user.getId()))
                .jwtID(UUID.randomUUID().toString())
                .claim(CLAIM_TYPE, type.name())
                .claim(CLAIM_VERSION, user.getTokenVersion())
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plus(ttl)))
                .build();
        try {
            SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS512), claims);
            jwt.sign(new MACSigner(secret));
            return jwt.serialize();
        } catch (JOSEException e) {
            throw new IllegalStateException("Không thể tạo token", e);
        }
    }

    public Optional<TokenInfo> parse(String token, TokenType expected) {
        try {
            SignedJWT jwt = SignedJWT.parse(token);
            if (!JWSAlgorithm.HS512.equals(jwt.getHeader().getAlgorithm())) {
                return Optional.empty();
            }
            if (!jwt.verify(new MACVerifier(secret))) {
                log.warn("Token signature invalid");
                return Optional.empty();
            }
            JWTClaimsSet claims = jwt.getJWTClaimsSet();
            Date exp = claims.getExpirationTime();
            if (exp == null || new Date().after(exp)) {
                return Optional.empty();
            }
            if (!ISSUER.equals(claims.getIssuer())
                    || !expected.name().equals(claims.getStringClaim(CLAIM_TYPE))) {
                return Optional.empty();
            }
            Integer version = claims.getIntegerClaim(CLAIM_VERSION);
            if (version == null) {
                return Optional.empty();
            }
            return Optional.of(new TokenInfo(Long.parseLong(claims.getSubject()), version));
        } catch (ParseException | JOSEException | NumberFormatException e) {
            log.debug("Token parse/verify failed: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
