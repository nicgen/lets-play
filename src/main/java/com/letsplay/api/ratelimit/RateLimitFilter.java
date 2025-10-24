package com.letsplay.api.ratelimit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.letsplay.api.dto.ErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Rate Limiting Filter for Authentication Endpoints
 *
 * Intercepts requests to authentication endpoints and applies rate limiting
 * to prevent brute force attacks. Uses client IP address as identifier.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    @Autowired
    private RateLimitService rateLimitService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        // Only apply rate limiting to authentication endpoints
        if (isAuthEndpoint(path)) {
            String clientIdentifier = getClientIdentifier(request);

            if (!rateLimitService.tryConsume(clientIdentifier)) {
                // Rate limit exceeded
                sendRateLimitExceededResponse(response);
                return;
            }
        }

        // Continue with the filter chain
        filterChain.doFilter(request, response);
    }

    /**
     * Checks if the request path is an authentication endpoint
     *
     * @param path Request URI path
     * @return true if path is an auth endpoint, false otherwise
     */
    private boolean isAuthEndpoint(String path) {
        return path != null && (
            path.startsWith("/api/auth/login") ||
            path.startsWith("/api/auth/register")
        );
    }

    /**
     * Extracts client identifier (IP address) from request
     *
     * Checks X-Forwarded-For header first (for proxied requests),
     * falls back to remote address.
     *
     * @param request HTTP request
     * @return Client IP address
     */
    private String getClientIdentifier(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");

        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // X-Forwarded-For may contain multiple IPs, take the first one
            return xForwardedFor.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }

    /**
     * Sends a 429 Too Many Requests response
     *
     * @param response HTTP response
     * @throws IOException if writing response fails
     */
    private void sendRateLimitExceededResponse(HttpServletResponse response) throws IOException {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.TOO_MANY_REQUESTS.value(),
            "Too many requests. Please try again later."
        );

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader("Retry-After", "60"); // Suggest retry after 60 seconds

        objectMapper.writeValue(response.getOutputStream(), error);
    }
}
