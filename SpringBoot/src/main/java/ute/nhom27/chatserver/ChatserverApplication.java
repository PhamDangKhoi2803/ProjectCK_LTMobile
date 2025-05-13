package ute.nhom27.chatserver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import ute.nhom27.chatserver.entity.User;
import ute.nhom27.chatserver.service.impl.UserService;

@SpringBootApplication
public class ChatserverApplication{

    @Autowired
    private UserService userService;

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encodedPassword = "$2a$10$oyIrgmrCkFpvo/C6s5386OQFz5Fw3P/TCd9fr5g0HVDFDtz3ZyGIy";

        boolean matches = encoder.matches("123456", encodedPassword);
        System.out.println("Password khớp? " + matches);
    }
}