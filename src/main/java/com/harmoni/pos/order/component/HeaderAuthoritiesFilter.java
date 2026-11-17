package com.harmoni.pos.order.component;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds the security context from the identity headers injected by the gateway.
 *
 * <p>The gateway validates the JWT and forwards {@code X-Username},
 * {@code X-Roles} and {@code X-Permissions}. This filter turns those headers
 * into an authenticated principal so {@code @PreAuthorize} can enforce the RBAC
 * permissions.</p>
 */
@Component
public class HeaderAuthoritiesFilter extends OncePerRequestFilter {

    private static final String HEADER_USERNAME = "X-Username";
    private static final String HEADER_ROLES = "X-Roles";
    private static final String HEADER_PERMISSIONS = "X-Permissions";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String username = request.getHeader(HEADER_USERNAME);
        if (username != null && !username.isBlank()
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            List<GrantedAuthority> authorities = new ArrayList<>();
            addCsv(authorities, request.getHeader(HEADER_ROLES), "ROLE_");
            addCsv(authorities, request.getHeader(HEADER_PERMISSIONS), "");
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(username, null, authorities));
        }
        filterChain.doFilter(request, response);
    }

    private void addCsv(List<GrantedAuthority> authorities, String csv, String prefix) {
        if (csv == null || csv.isBlank()) {
            return;
        }
        for (String value : csv.split(",")) {
            String trimmed = value.trim();
            if (!trimmed.isEmpty()) {
                authorities.add(new SimpleGrantedAuthority(prefix + trimmed));
            }
        }
    }
}
