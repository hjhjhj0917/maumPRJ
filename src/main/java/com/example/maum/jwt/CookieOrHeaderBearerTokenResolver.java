package com.example.maum.jwt;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;

public class CookieOrHeaderBearerTokenResolver implements BearerTokenResolver {

    private final String cookieName;

    private final DefaultBearerTokenResolver delegate;

    public CookieOrHeaderBearerTokenResolver(String cookieName) {
        this.cookieName = cookieName;
        this.delegate = new DefaultBearerTokenResolver();
        this.delegate.setAllowFormEncodedBodyParameter(false);
        this.delegate.setAllowUriQueryParameter(false);
    }

    @Override
    public String resolve(HttpServletRequest request) {

        String fromCookie = extractFormCookie(request);
        if (fromCookie != null && !fromCookie.isEmpty()) {
            return fromCookie;
        }
        return delegate.resolve(request);
    }

    private String extractFormCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;

        for (Cookie c : cookies) {
            if (cookieName.equals(c.getName())) {
                String v = c.getValue();
                if (v == null) return null;

                v = v.trim();
                if (v.isEmpty()) return null;

                if ((v.startsWith("\"") && v.endsWith("\"")) || (v.startsWith("'") && v.endsWith("'"))) {
                    v = v.substring(1, v.length() - 1).trim();
                }

                final String BEARER_PREFIX = "bearer ";
                if (v.length() > BEARER_PREFIX.length() && v.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
                    v = v.substring(BEARER_PREFIX.length()).trim();
                }

                return v.isEmpty() ? null : v;
            }
        }
        return null;
    }
}
