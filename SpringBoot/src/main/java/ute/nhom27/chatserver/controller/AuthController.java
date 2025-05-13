package ute.nhom27.chatserver.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import ute.nhom27.chatserver.dto.*;
import ute.nhom27.chatserver.entity.User;
import ute.nhom27.chatserver.repository.UserRepository;
import ute.nhom27.chatserver.service.impl.UserDetailsServiceImpl;
import ute.nhom27.chatserver.service.impl.UserService;
import ute.nhom27.chatserver.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        logger.info("Login attempt for user: {}", loginRequest.getPhoneOrEmail());
        logger.info("Login attempt for password: {}", loginRequest.getPassword());
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getPhoneOrEmail(), loginRequest.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);


            User user = userDetailsService.getUserByPhoneOrEmail(loginRequest.getPhoneOrEmail());
            final String jwt = jwtUtil.generateToken(loginRequest.getPhoneOrEmail());
            if (user == null) {
                logger.error("User not found: {}", loginRequest.getPhoneOrEmail());
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("{\"error\": \"User not found\"}");
            }

            UserDTO userDTO = userService.convertToDTO(user);

            LoginResponse loginResponse = new LoginResponse(userDTO, jwt);
            logger.info("User logged in: {}", user.getUsername());
            return ResponseEntity.ok(loginResponse);
        } catch (Exception e) {
            logger.error("Login error for phoneOrEmail: {}, error: {}", loginRequest.getPhoneOrEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"error\": \"Invalid credentials: " + e.getMessage() + "\"}");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        logger.info("Register attempt with username: {}", registerRequest.getUsername());
        try {
            // Kiểm tra dữ liệu đầu vào
            if (registerRequest.getUsername() == null || registerRequest.getUsername().isEmpty() ||
                    registerRequest.getEmail() == null || registerRequest.getEmail().isEmpty() ||
                    registerRequest.getPhone() == null || registerRequest.getPhone().isEmpty() ||
                    registerRequest.getPassword() == null || registerRequest.getPassword().isEmpty()) {
                logger.error("Invalid request: required fields are missing");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("{\"error\": \"Username, email, phone, and password are required\"}");
            }

            // Kiểm tra trùng lặp
            if (userService.getUserByUsername(registerRequest.getUsername()).isPresent()) {
                logger.error("Username already exists: {}", registerRequest.getUsername());
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("{\"error\": \"Username already exists\"}");
            }
            if (userService.getUserByEmail(registerRequest.getEmail()).isPresent()) {
                logger.error("Email already exists: {}", registerRequest.getEmail());
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("{\"error\": \"Email already exists\"}");
            }
            if (userService.getUserByPhone(registerRequest.getPhone()).isPresent()) {
                logger.error("Phone already exists: {}", registerRequest.getPhone());
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("{\"error\": \"Phone already exists\"}");
            }

            // Tạo User entity
            User user = new User();
            user.setUsername(registerRequest.getUsername());
            user.setEmail(registerRequest.getEmail());
            user.setPhone(registerRequest.getPhone());
            user.setPassword(registerRequest.getPassword()); // Sẽ được mã hóa trong saveUser
            user.setPublicKey(registerRequest.getPublicKey());
            user.setNotificationToken(registerRequest.getNotificationToken());

            user.setThemePreference(registerRequest.getThemePreference() != null ? registerRequest.getThemePreference() : "light");

            // Lưu người dùng
            user = userService.saveUser(user);

            final String jwt = jwtUtil.generateToken(user.getPhone());

            // Tạo UserDTO
            UserDTO userDTO = userService.convertToDTO(user);

            logger.info("Registration successful for user: {}", user.getUsername());
            return ResponseEntity.ok(new LoginResponse(userDTO, jwt));
        } catch (Exception e) {
            logger.error("Registration error for username: {}, error: {}", registerRequest.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Registration failed: " + e.getMessage() + "\"}");
        }
    }

    @PutMapping("/update-theme")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateTheme(@RequestBody ThemeUpdateRequest themeUpdateRequest, Authentication authentication) {
        logger.info("Update theme attempt");
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                logger.error("Authentication is null or not authenticated");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("{\"error\": \"Unauthorized: No valid authentication provided\"}");
            }

            String phone = authentication.getName(); // Lấy phone từ token
            logger.info("Update theme for phone: {}", phone);

            User user = userRepository.findByPhoneOrEmail(phone, phone).orElse(null);
            if (user == null) {
                logger.error("User not found with phone: {}", phone);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("{\"error\": \"User not found\"}");
            }

            if (themeUpdateRequest.getThemePreference() == null || themeUpdateRequest.getThemePreference().isEmpty()) {
                logger.error("Invalid theme preference");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("{\"error\": \"Theme preference is required\"}");
            }

            user.setThemePreference(themeUpdateRequest.getThemePreference());
            userService.saveUser(user);

            UserDTO userDTO = userService.convertToDTO(user);

            logger.info("Theme updated for user: {}", user.getUsername());
            return ResponseEntity.ok(userDTO);
        } catch (Exception e) {
            logger.error("Theme update error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Theme update failed: " + e.getMessage() + "\"}");
        }
    }
}
