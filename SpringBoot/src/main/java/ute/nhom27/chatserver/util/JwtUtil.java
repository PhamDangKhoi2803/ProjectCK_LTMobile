package ute.nhom27.chatserver.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);
    private static final String SECRET_KEY = "111111111111111111111111111111"; // Thay bằng khóa bí mật an toàn hơn
    private static final long EXPIRATION_TIME = 86400000; // 1 ngày (ms)

    public String generateToken(String phone) {
        logger.debug("Generating token for phone: {}", phone);
        return Jwts.builder()
                .setSubject(phone)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, SECRET_KEY)
                .compact();
    }

    public String extractUsername(String token) {
        logger.debug("Extracting username from token");
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateToken(String token, String phoneOrEmail) {
        logger.debug("Validating token for phoneOrEmail: {}", phoneOrEmail);
        try {
            final String extractedPhoneOrEmail = extractUsername(token);
            boolean isValid = extractedPhoneOrEmail.equals(phoneOrEmail) && !isTokenExpired(token);
            logger.debug("Token valid: {}, extracted phoneOrEmail: {}", isValid, extractedPhoneOrEmail);
            return isValid;
        } catch (Exception e) {
            logger.error("Token validation error: {}", e.getMessage());
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        logger.debug("Checking token expiration");
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody()
                .getExpiration()
                .before(new Date());
    }
}