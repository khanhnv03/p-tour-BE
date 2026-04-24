package com.ptit.tour.common.ratelimit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ptit.tour.common.response.ApiResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Map<String, Rule> RULES = Map.of(
        "POST /auth/login", new Rule(5, Duration.ofMinutes(1)),
        "POST /auth/forgot-password", new Rule(3, Duration.ofMinutes(10)),
        "POST /auth/reset-password", new Rule(5, Duration.ofMinutes(10)),
        "GET /deals/apply", new Rule(20, Duration.ofMinutes(1))
    );

    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        Rule rule = RULES.get(request.getMethod() + " " + request.getServletPath());
        if (rule == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String key = request.getMethod() + ":" + request.getServletPath() + ":" + clientKey(request);
        Bucket bucket = buckets.compute(key, (ignored, current) -> {
            Instant now = Instant.now();
            if (current == null || !current.windowStart.plus(rule.window()).isAfter(now)) {
                return new Bucket(now, 1);
            }
            current.count++;
            return current;
        });

        if (bucket.count > rule.limit()) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            objectMapper.writeValue(response.getWriter(),
                ApiResponse.error("RATE_LIMITED", "Bạn thao tác quá nhanh, vui lòng thử lại sau"));
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String clientKey(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private record Rule(int limit, Duration window) {
    }

    private static final class Bucket {
        private final Instant windowStart;
        private int count;

        private Bucket(Instant windowStart, int count) {
            this.windowStart = windowStart;
            this.count = count;
        }
    }
}
