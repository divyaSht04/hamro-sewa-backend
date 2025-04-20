package project.hamrosewa.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import project.hamrosewa.exceptions.JwtTokenException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JWTUtil {
    private static final String SECRET_KEY = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    public String generateToken(String username) {
        try {
            Map<String, Object> claims = new HashMap<>();
            claims.put("email", username);

            return Jwts.builder()
                    .claims(claims)
                    .subject(username)
                    .issuedAt(new Date(System.currentTimeMillis()))
                    .expiration(new Date(System.currentTimeMillis() + 60 * 60 * 1000 * 2)) // 2 hour
                    .signWith(getKey())
                    .compact();
        } catch (Exception e) {
            throw new JwtTokenException("Failed to generate JWT token");
        }
    }

    private SecretKey getKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractEmail(String token) {
        try {
            return extractClaim(token, Claims::getSubject);
        } catch (ExpiredJwtException e) {
            throw new JwtTokenException("JWT token has expired");
        } catch (JwtException e) {
            throw new JwtTokenException("Invalid JWT token");
        }
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
        final Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new JwtTokenException("JWT token has expired");
        } catch (JwtException e) {
            throw new JwtTokenException("Failed to parse JWT token");
        }
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String userName = extractEmail(token);
            return (userName.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (ExpiredJwtException e) {
            throw new JwtTokenException("JWT token has expired");
        } catch (JwtException e) {
            throw new JwtTokenException("Invalid JWT token");
        }
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public  Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}
