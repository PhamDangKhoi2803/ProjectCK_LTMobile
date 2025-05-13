package ute.nhom27.chatserver.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import ute.nhom27.chatserver.dto.LoginRequest;
import ute.nhom27.chatserver.dto.LoginResponse;
import ute.nhom27.chatserver.dto.RegisterRequest;
import ute.nhom27.chatserver.dto.UserDTO;
import ute.nhom27.chatserver.entity.User;
import ute.nhom27.chatserver.service.impl.UserDetailsServiceImpl;
import ute.nhom27.chatserver.service.impl.UserService;
import ute.nhom27.chatserver.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        logger.info("Login attempt with phoneOrEmail: {}", loginRequest.getPhoneOrEmail());
        try {
            if (loginRequest.getPhoneOrEmail() == null || loginRequest.getPassword() == null) {
                logger.error("Invalid request: phoneOrEmail or password is null");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("{\"error\": \"phoneOrEmail and password are required\"}");
            }

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getPhoneOrEmail(),
                            loginRequest.getPassword()
                    )
            );
            logger.info("Authentication successful for phoneOrEmail: {}", loginRequest.getPhoneOrEmail());

            final UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getPhoneOrEmail());
            final String jwt = jwtUtil.generateToken(userDetails.getUsername());

            User user = userDetailsService.getUserByPhoneOrEmail(loginRequest.getPhoneOrEmail());
            if (user == null) {
                logger.error("User not found after authentication for phoneOrEmail: {}", loginRequest.getPhoneOrEmail());
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("{\"error\": \"User not found\"}");
            }

            UserDTO userDTO = userService.convertToDTO(user);

            logger.info("Login successful for user: {}", user.getUsername());
            return ResponseEntity.ok(new LoginResponse(userDTO, jwt));
        } catch (Exception e) {
            logger.error("Login error for phoneOrEmail: {}, error: {}", loginRequest.getPhoneOrEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Login failed: " + e.getMessage() + "\"}");
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

            // Tạo JWT token
            final UserDetails userDetails = userDetailsService.loadUserByUsername(user.getPhone());
            final String jwt = jwtUtil.generateToken(userDetails.getUsername());

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
}
