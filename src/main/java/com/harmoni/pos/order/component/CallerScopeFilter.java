package com.harmoni.pos.order.component;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Resolves the caller's store scope once per report request and publishes it on
 * {@link CallerScopeContext}.
 *
 * <p>Only report paths are resolved: expanding a brand or chain assignment
 * needs a call to the menu service, and there is no reason to pay for it on
 * order endpoints that do not use the scope.</p>
 */
@Slf4j
@RequiredArgsConstructor
public class CallerScopeFilter extends OncePerRequestFilter {

    private static final String REPORT_PATH = "/api/v1/reports/";

    private final CallerScopeService callerScopeService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith(REPORT_PATH);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            CallerScopeContext.set(resolve(request));
            filterChain.doFilter(request, response);
        } finally {
            CallerScopeContext.clear();
        }
    }

    private CallerScope resolve(HttpServletRequest request) {
        try {
            return callerScopeService.resolve(
                    request.getHeader(CallerScopeService.HEADER_SCOPES),
                    request.getHeader(CallerScopeService.HEADER_USERNAME));
        } catch (RuntimeException exception) {
            log.error("Could not resolve caller scope", exception);
            return CallerScope.none();
        }
    }
}
