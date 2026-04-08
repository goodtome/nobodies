package com.nobodies.platform.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nobodies.platform.common.api.ApiResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
public class AuthRateLimitFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;
    private final Map<String, RateLimitRecord> requestCounters = new ConcurrentHashMap<>();

    @Value("${auth.rate-limit.window-seconds}")
    private long windowSeconds;

    @Value("${auth.rate-limit.max-requests}")
    private int maxRequests;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return !uri.startsWith("/auth/login")
            && !uri.startsWith("/auth/register")
            && !uri.startsWith("/auth/password-reset/request");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        String clientKey = request.getRemoteAddr() + ":" + request.getRequestURI();
        long now = Instant.now().getEpochSecond();
        RateLimitRecord record = requestCounters.compute(clientKey, (key, current) -> {
            if (current == null || now - current.windowStartEpochSeconds >= windowSeconds) {
                return new RateLimitRecord(now, new AtomicInteger(1));
            }
            current.counter.incrementAndGet();
            return current;
        });

        if (record.counter.get() > maxRequests) {
            response.setStatus(429);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.fail("Too many auth attempts, please try again later.")));
            return;
        }

        filterChain.doFilter(request, response);
    }

    private record RateLimitRecord(long windowStartEpochSeconds, AtomicInteger counter) {
    }
}
