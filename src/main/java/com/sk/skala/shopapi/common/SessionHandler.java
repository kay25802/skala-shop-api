package com.sk.skala.shopapi.common;

import com.sk.skala.shopapi.exception.Error;
import com.sk.skala.shopapi.exception.ResponseException;
import com.sk.skala.shopapi.tools.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
@RequiredArgsConstructor
public class SessionHandler {

    private static final String ACCESS_COOKIE = "bff-access";
    private final JwtUtil jwtUtil;

    public void storeAccessToken(String customerId) {
        ServletRequestAttributes attrs = currentAttributes();
        HttpServletResponse response = attrs.getResponse();
        if (response == null) {
            throw new ResponseException(Error.NOT_AUTHENTICATED);
        }

        Cookie cookie = new Cookie(ACCESS_COOKIE, jwtUtil.createToken(customerId));
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60);
        response.addCookie(cookie);
    }

    public String getCustomerId() {
        HttpServletRequest request = currentAttributes().getRequest();
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            throw new ResponseException(Error.NOT_AUTHENTICATED);
        }

        for (Cookie cookie : cookies) {
            if (ACCESS_COOKIE.equals(cookie.getName())) {
                try {
                    return jwtUtil.getCustomerId(cookie.getValue());
                } catch (Exception e) {
                    throw new ResponseException(Error.NOT_AUTHENTICATED);
                }
            }
        }
        throw new ResponseException(Error.NOT_AUTHENTICATED);
    }

    private ServletRequestAttributes currentAttributes() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs) {
            return attrs;
        }
        throw new ResponseException(Error.NOT_AUTHENTICATED);
    }
}
