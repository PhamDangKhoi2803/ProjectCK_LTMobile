package ute.nhom27.chatserver.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import ute.nhom27.chatserver.dto.LoginRequest;
import ute.nhom27.chatserver.dto.LoginResponse;
import ute.nhom27.chatserver.dto.UserDTO;
import ute.nhom27.chatserver.entity.User;
import ute.nhom27.chatserver.service.impl.UserDetailsServiceImpl;
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

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest loginRequest) throws Exception {
        logger.info("Login attempt with phoneOrEmail: {}", loginRequest.getPhoneOrEmail());
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getPhoneOrEmail(),
                            loginRequest.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            logger.error("Invalid credentials for phoneOrEmail: {}", loginRequest.getPhoneOrEmail());
            throw new Exception("Invalid credentials", e);
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getPhoneOrEmail());
        final String jwt = jwtUtil.generateToken(userDetails.getUsername());

        // Tạo UserDTO để trả về
        User user = userDetailsService.getUserByPhoneOrEmail(loginRequest.getPhoneOrEmail());
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setUsername(user.getUsername());
        userDTO.setEmail(user.getEmail());
        userDTO.setPhone(user.getPhone());
        userDTO.setPublicKey(user.getPublicKey());
        userDTO.setNotificationToken(user.getNotificationToken());
        userDTO.setThemePreference(user.getThemePreference());

        logger.info("Login successful for user: {}", user.getUsername());
        return new LoginResponse(userDTO, jwt);
    }
}
