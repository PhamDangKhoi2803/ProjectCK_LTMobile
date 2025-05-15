package ute.nhom27.chatserver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import ute.nhom27.chatserver.dto.GroupMemberDTO;
import ute.nhom27.chatserver.entity.User;
import ute.nhom27.chatserver.service.IGroupService;
import ute.nhom27.chatserver.service.impl.GroupService;
import ute.nhom27.chatserver.service.impl.UserService;

import java.util.List;

@SpringBootApplication
public class ChatserverApplication {

    @Autowired
    private GroupService groupService;

    public static void main(String[] args) {
        SpringApplication.run(ChatserverApplication.class, args);
    }

}