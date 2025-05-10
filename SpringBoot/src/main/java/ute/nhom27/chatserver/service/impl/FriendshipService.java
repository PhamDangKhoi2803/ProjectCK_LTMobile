package ute.nhom27.chatserver.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ute.nhom27.chatserver.entity.Friendship;
import ute.nhom27.chatserver.entity.User;
import ute.nhom27.chatserver.repository.FriendshipRepository;
import ute.nhom27.chatserver.repository.UserRepository;
import ute.nhom27.chatserver.service.IFriendshipService;

import java.util.Optional;

@Service
public class FriendshipService implements IFriendshipService {

    @Autowired
    private FriendshipRepository friendshipRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public boolean sendFriendRequest(Long senderId, Long receiverId) {
        if (senderId.equals(receiverId)) return false;

        Optional<User> sender = userRepository.findById(senderId);
        Optional<User> receiver = userRepository.findById(receiverId);

        if (sender.isEmpty() || receiver.isEmpty()) return false;

        boolean exists = friendshipRepository.existsByUserIdAndFriendId(senderId, receiverId);
        if (exists) return false;

        Friendship friendship = new Friendship();
        friendship.setUser(sender.get());
        friendship.setFriend(receiver.get());
        friendship.setStatus("PENDING");
        friendshipRepository.save(friendship);

        return true;
    }

    @Override
    public boolean acceptFriendRequest(Long senderId, Long receiverId) {
        Optional<Friendship> request = friendshipRepository.findByUserIdAndFriendId(senderId, receiverId);
        if (request.isEmpty() || !"PENDING".equals(request.get().getStatus())) return false;

        Friendship friendship = request.get();
        friendship.setStatus("ACCEPTED");
        friendshipRepository.save(friendship);

        // Thêm bản ghi phản hồi
        Friendship reverse = new Friendship();
        reverse.setUser(friendship.getFriend());
        reverse.setFriend(friendship.getUser());
        reverse.setStatus("ACCEPTED");
        friendshipRepository.save(reverse);

        return true;
    }

    @Override
    public boolean rejectFriendRequest(Long senderId, Long receiverId) {
        Optional<Friendship> request = friendshipRepository.findByUserIdAndFriendId(senderId, receiverId);
        if (request.isEmpty() || !"PENDING".equals(request.get().getStatus())) return false;

        friendshipRepository.delete(request.get());
        return true;
    }

    @Override
    public boolean areFriends(Long userId1, Long userId2) {
        Optional<Friendship> friendship = friendshipRepository.findByUserIdAndFriendId(userId1, userId2);
        return friendship.isPresent() && "ACCEPTED".equals(friendship.get().getStatus());
    }
}