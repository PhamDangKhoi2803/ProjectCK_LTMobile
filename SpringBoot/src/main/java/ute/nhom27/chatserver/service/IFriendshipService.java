package ute.nhom27.chatserver.service;

import ute.nhom27.chatserver.entity.Friendship;

public interface IFriendshipService {
    boolean sendFriendRequest(Long senderId, Long receiverId);
    boolean acceptFriendRequest(Long senderId, Long receiverId);
    boolean rejectFriendRequest(Long senderId, Long receiverId);

    boolean areFriends(Long userId1, Long userId2);
}