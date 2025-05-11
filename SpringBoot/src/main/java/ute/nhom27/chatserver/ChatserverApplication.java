package ute.nhom27.chatserver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ute.nhom27.chatserver.entity.User;
import ute.nhom27.chatserver.service.impl.UserService;

@SpringBootApplication
public class ChatserverApplication implements CommandLineRunner {

    @Autowired
    private UserService userService;

    public static void main(String[] args) {
        SpringApplication.run(ChatserverApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        // Tạo người dùng thử nghiệm
//        User user = new User();
//        user.setUsername("testuser2");
//        user.setEmail("khoi@gmail.com");
//        user.setPhone("0123456789");
//        user.setPassword("123123"); // Sẽ được mã hóa
//        user.setThemePreference("light");
//        userService.saveUser(user);
    }
}