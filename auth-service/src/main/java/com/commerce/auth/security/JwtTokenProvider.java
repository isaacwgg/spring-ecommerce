package com.commerce.auth.security;

import com.commerce.auth.config.JwtProperties;
import com.commerce.auth.models.UserDAO;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {
  
  private final Key key;
  private final JwtProperties jwtProperties;
  
  public JwtTokenProvider(JwtProperties jwtProperties
  ) {
    this.jwtProperties = jwtProperties;
    this.key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.ISO_8859_1));
  }
  
  public String createToken(UserDAO userDAO) {
    Date now = new Date();
    Date expiry = new Date(now.getTime() + jwtProperties.getExpiration());
    
    return Jwts.builder()
        .setSubject(userDAO.getId().toString())
        .claim("username", userDAO.getUsername())
        .setIssuedAt(now)
        .setExpiration(expiry)
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
  }
  
  public Jws<Claims> validate(String token) throws JwtException {
    return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
  }
  
  public String getUsername(String token) {
    return validate(token).getBody().get("username", String.class);
  }
  
  public Long getUserId(String token) {
    return Long.valueOf(validate(token).getBody().getSubject());
  }
}
