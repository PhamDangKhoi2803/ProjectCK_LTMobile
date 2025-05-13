package ute.nhom27.chatserver.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ute.nhom27.chatserver.service.impl.UserDetailsServiceImpl;
import ute.nhom27.chatserver.service.impl.UserService;
import ute.nhom27.chatserver.util.JwtUtil;

import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtRequestFilter.class);

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        // Bỏ qua xác thực cho các endpoint không cần token
        if (request.getRequestURI().startsWith("/auth/")) {
            logger.info("Processing request: {}", request.getRequestURI());
            String requestURI = request.getRequestURI();
            if (requestURI.equals("/auth/login") || requestURI.equals("/auth/register")) {
                logger.info("Skipping authentication for: {}", requestURI);
                chain.doFilter(request, response);
                return;
            }
        }

        final String authorizationHeader = request.getHeader("Authorization");
        logger.info("Authorization header: {}", authorizationHeader);

        String phoneOrEmail = null; // Sử dụng phoneOrEmail để nhất quán với logic đăng nhập
        String jwt = null;

        // Trích xuất token và phoneOrEmail từ header
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            try {
                phoneOrEmail = jwtUtil.extractUsername(jwt); // Trích xuất phoneOrEmail từ token
                logger.info("Extracted phoneOrEmail from token: {}", phoneOrEmail);
            } catch (Exception e) {
                logger.error("Error extracting phoneOrEmail from JWT: {}", e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Invalid token: " + e.getMessage() + "\"}");
                return; // Dừng xử lý nếu token không hợp lệ
            }
        } else {
            logger.debug("No Bearer token found in Authorization header");
        }

        // Xác thực nếu phoneOrEmail tồn tại và chưa có Authentication
        if (phoneOrEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = userDetailsService.loadUserByUsername(phoneOrEmail);
                if (jwtUtil.validateToken(jwt, phoneOrEmail)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    logger.info("Authentication successful for phoneOrEmail: {}", phoneOrEmail);
                } else {
                    logger.error("Token validation failed for phoneOrEmail: {}", phoneOrEmail);
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\": \"Token validation failed\"}");
                    return; // Dừng xử lý nếu token không hợp lệ
                }
            } catch (UsernameNotFoundException e) {
                logger.warn("User not found with phoneOrEmail: {}", phoneOrEmail);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"User not found with phoneOrEmail: " + phoneOrEmail + "\"}");
                return; // Dừng xử lý nếu không tìm thấy người dùng
            } catch (Exception e) {
                logger.error("Authentication error for phoneOrEmail: {}, error: {}", phoneOrEmail, e.getMessage());
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Authentication error: " + e.getMessage() + "\"}");
                return; // Dừng xử lý nếu có lỗi khác
            }
        }

        chain.doFilter(request, response);
    }
}
