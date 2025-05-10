package ute.nhom27.chatserver.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ute.nhom27.chatserver.entity.ChatMessage;
import ute.nhom27.chatserver.entity.GroupMessage;
import ute.nhom27.chatserver.service.IMessageService;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    @Autowired
    private IMessageService messageService;

    @GetMapping("/private")
    public List<ChatMessage> getPrivateMessages(
            @RequestParam Long userId1,
            @RequestParam Long userId2
    ) {
        return messageService.getPrivateMessages(userId1, userId2);
    }

    @GetMapping("/group/{groupId}")
    public List<GroupMessage> getGroupMessages(@PathVariable Long groupId) {
        return messageService.getGroupMessages(groupId);
    }
}