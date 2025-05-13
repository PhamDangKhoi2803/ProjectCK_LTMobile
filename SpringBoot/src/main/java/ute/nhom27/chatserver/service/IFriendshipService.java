package ute.nhom27.chatserver.service;

import ute.nhom27.chatserver.dto.UserDTO;
import ute.nhom27.chatserver.entity.Friendship;

import java.util.List;

public interface IFriendshipService {
    boolean sendFriendRequest(Long senderId, Long receiverId);
    boolean acceptFriendRequest(Long senderId, Long receiverId);
    boolean rejectFriendRequest(Long senderId, Long receiverId);

    boolean areFriends(Long userId1, Long userId2);

    List<UserDTO> getFriends(Long userId);
}