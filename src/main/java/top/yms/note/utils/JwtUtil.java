package top.yms.note.utils;

import io.jsonwebtoken.*;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.yms.note.comm.Constants;
import top.yms.note.config.SpringContext;
import top.yms.note.conpont.NoteCache;


/**
 * Created by yangmingsen on 2024/8/19.
 */
public class JwtUtil {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    private final static String SECRET_KEY = "note-secret-key";


    //12h
    private final static Long expireTime = 12 * 60 * 60 * 1000L;


    // 生成JWT
    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, username);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expireTime))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    // 验证JWT
    public Boolean validateToken(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }


    // 验证JWT
    public Boolean validateToken(String token) {
        try {
            return !isTokenExpired(token);
        } catch (SignatureException | MalformedJwtException e) {
            // 如果捕获到SignatureException或MalformedJwtException，说明令牌被篡改或格式错误
            logger.error("JWT Signature does not match or Token is Malformed", e);
            return false;
        } catch (ExpiredJwtException e) {
            // 令牌过期
            logger.error("JWT Token has expired", e);
            return false;
        }
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

}
