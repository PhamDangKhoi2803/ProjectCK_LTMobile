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

    @Bean
    public CommandLineRunner commandLineRunner() {
        return args -> {
            System.out.println("\n========== TESTING GROUP MEMBERS ==========");

            // Test với groupId = 4
            Long groupId = 4L;
            System.out.println("Getting members for group ID: " + groupId);

            List<GroupMemberDTO> members = groupService.getGroupMembersWithInfo(groupId);

            System.out.println("\nTotal members found: " + members.size());
            System.out.println("\nMember Details:");

            for (GroupMemberDTO member : members) {
                System.out.println("\n-------------------");
                System.out.println("Group ID: " + member.getGroupId());
                System.out.println("User ID: " + member.getUserId());
                System.out.println("Name: " + member.getName());
                System.out.println("Role: " + member.getRole());
                System.out.println("Avatar: " + member.getAvatar());
            }

            System.out.println("\n========== TEST COMPLETED ==========\n");
        };
    }
}