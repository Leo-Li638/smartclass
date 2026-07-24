package com.smartclass.interceptor;

import com.smartclass.common.Constants;
import com.smartclass.util.JwtUtil;
import com.smartclass.util.UserContext;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录鉴权拦截器:
 * 1. 校验 token 是否有效
 * 2. 按 /api/admin、/api/teacher、/api/student 前缀做角色隔离
 */
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        Claims claims = jwtUtil.parseToken(token);
        if (claims == null) {
            response.setStatus(200);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"未登录或登录已过期\",\"data\":null}");
            return false;
        }
        Long userId = claims.get("userId", Long.class);
        String role = claims.get("role", String.class);
        String uri = request.getRequestURI();

        if (uri.startsWith("/api/admin") && !Constants.ROLE_ADMIN.equals(role)) {
            return reject(response);
        }
        if (uri.startsWith("/api/teacher") && !Constants.ROLE_TEACHER.equals(role)
                && !Constants.ROLE_ADMIN.equals(role)) {
            return reject(response);
        }
        if (uri.startsWith("/api/student") && !Constants.ROLE_STUDENT.equals(role)) {
            return reject(response);
        }
        UserContext.set(userId, role);
        return true;
    }

    private boolean reject(HttpServletResponse response) throws Exception {
        response.setStatus(200);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":403,\"message\":\"无权限访问\",\"data\":null}");
        return false;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }
}
