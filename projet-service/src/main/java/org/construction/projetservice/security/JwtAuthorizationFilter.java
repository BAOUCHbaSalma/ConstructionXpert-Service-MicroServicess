package org.construction.projetservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.Key;
import java.util.List;
import java.util.stream.Collectors;

public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private static final String SECRET_KEY_BASE64 = "357638792F423F4428472B4B6250655368566D597133743677397A2443264629";

    private static final Key SECRET_KEY = Keys.hmacShaKeyFor(java.util.Base64.getDecoder().decode(SECRET_KEY_BASE64));

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authorizationToken = request.getHeader("Authorization");

        if (authorizationToken != null && authorizationToken.startsWith("Bearer ")) {
            try {
                String jwt = authorizationToken.substring(7);
                Claims claims = Jwts.parser()
                        .setSigningKey(SECRET_KEY)
                        .build()
                        .parseClaimsJws(jwt)
                        .getBody();

                String username = claims.getSubject();
                List<String> roles = (List<String>) claims.get("roles");

                List<SimpleGrantedAuthority> authorities = roles.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .collect(Collectors.toList());

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        authorities
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception e) {

                e.printStackTrace();
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid JWT token");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
