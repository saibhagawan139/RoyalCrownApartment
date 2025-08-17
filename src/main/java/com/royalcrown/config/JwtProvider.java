//package com.royalcrown.config;
//
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.SignatureAlgorithm;
//import io.jsonwebtoken.security.Keys;
//import javax.crypto.SecretKey;
//import org.springframework.stereotype.Component;
//
//import java.util.Date;
//
//@Component
//public class JwtProvider {
//
//    private final SecretKey secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
//    private final long validityMs = 3600000;
//
//    public String generateToken(String username, String role) {
//        Date now = new Date();
//        Date expiry = new Date(now.getTime() + validityMs);
//
//        return Jwts.builder()
//                .setSubject(username)
//                .claim("role", role)
//                .setIssuedAt(now)
//                .setExpiration(expiry)
//                .signWith(secretKey, SignatureAlgorithm.HS256)
//                .compact();
//    }
//
//    public boolean validate(String token) {
//        try {
//            Jwts.parserBuilder()
//                .setSigningKey(secretKey)
//                .build()
//                .parseClaimsJws(token);
//            return true;
//        } catch (Exception ex) {
//            // Log or handle exception if needed
//            return false;
//        }
//    }
//
//    public String getUsername(String token) {
//        return Jwts.parserBuilder()
//                .setSigningKey(secretKey)
//                .build()
//                .parseClaimsJws(token)
//                .getBody()
//                .getSubject();
//    }
//
//    public String getRole(String token) {
//        Object role = Jwts.parserBuilder()
//                .setSigningKey(secretKey)
//                .build()
//                .parseClaimsJws(token)
//                .getBody()
//                .get("role");
//        return (role != null) ? role.toString() : null;
//    }
//}

package com.royalcrown.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtProvider {

    private SecretKey secretKey;
    private final long validityMs = 3600000; // 1 hour

    @PostConstruct
    public void init() {
        secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    }

    public String generateToken(String username, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + validityMs);

        return Jwts.builder()
                .setSubject(username)
                .claim("role", role.toUpperCase())
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validate(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public String getRole(String token) {
        Object role = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("role");
        return role != null ? role.toString().toUpperCase() : null;
    }
}

