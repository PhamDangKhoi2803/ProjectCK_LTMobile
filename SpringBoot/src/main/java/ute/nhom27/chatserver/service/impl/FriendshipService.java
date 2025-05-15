package ute.nhom27.chatserver.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ute.nhom27.chatserver.dto.UserDTO;
import ute.nhom27.chatserver.entity.Friendship;
import ute.nhom27.chatserver.entity.User;
import ute.nhom27.chatserver.repository.FriendshipRepository;
import ute.nhom27.chatserver.repository.UserRepository;
import ute.nhom27.chatserver.service.IFriendshipService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class FriendshipService implements IFriendshipService {

    @Autowired
    private FriendshipRepository friendshipRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Override
    public boolean sendFriendRequest(Long senderId, Long receiverId) {
        if (senderId.equals(receiverId)) return false;

        Optional<User> sender = userRepository.findById(senderId);
        Optional<User> receiver = userRepository.findById(receiverId);

        if (sender.isEmpty() || receiver.isEmpty()) return false;

        //Kiểm tra 2 phía
        boolean exists = friendshipRepository.existsByUserIdAndFriendId(senderId, receiverId);
        if (exists) return false;

        boolean exists2 = friendshipRepository.existsByUserIdAndFriendId(receiverId, senderId);
        if (exists2) return false;

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
        // Kiểm tra cả 2 chiều của mối quan hệ bạn bè
        Optional<Friendship> friendship1 = friendshipRepository.findByUserIdAndFriendId(userId1, userId2);
        Optional<Friendship> friendship2 = friendshipRepository.findByUserIdAndFriendId(userId2, userId1);

        // Trả về true nếu một trong hai chiều tồn tại và có status là ACCEPTED
        return (friendship1.isPresent() && "ACCEPTED".equals(friendship1.get().getStatus())) ||
                (friendship2.isPresent() && "ACCEPTED".equals(friendship2.get().getStatus()));
    }

    @Override
    public List<UserDTO> getFriends(Long userId) {
        List<Friendship> friendships = friendshipRepository.findAcceptedFriendships(userId);
        List<UserDTO> friends = new ArrayList<>();

        for (Friendship f : friendships) {
            User friend = f.getUser().getId().equals(userId) ? f.getFriend() : f.getUser();
            friends.add(userService.convertToDTO(friend));
        }

        return friends;
    }

    @Override
    public List<UserDTO> getFriendRequests(Long userId) {
        // Lấy danh sách Friendship với trạng thái PENDING mà userId là người nhận (receiver)
        List<Friendship> pendingRequests = friendshipRepository.findByFriendIdAndStatus(userId, "PENDING");
        List<UserDTO> requestSenders = new ArrayList<>();

        for (Friendship f : pendingRequests) {
            User sender = f.getUser(); // Người gửi lời mời là user
            requestSenders.add(userService.convertToDTO(sender));
        }

        return requestSenders;
    }

    @Override
    public List<UserDTO> getSentFriendRequests(Long userId) {
        // Lấy danh sách Friendship với trạng thái PENDING mà userId là người gửi (sender)
        List<Friendship> sentRequests = friendshipRepository.findByUserIdAndStatus(userId, "PENDING");
        List<UserDTO> receivers = new ArrayList<>();

        for (Friendship f : sentRequests) {
            User receiver = f.getFriend(); // Người nhận lời mời là friend
            receivers.add(userService.convertToDTO(receiver));
        }

        return receivers;
    }

    @Override
    public List<UserDTO> getNonFriendUsers(Long userId) {
        List<User> allUsers = userRepository.findAll();
        List<UserDTO> nonFriends = new ArrayList<>();

        for (User user : allUsers) {
            if (user.getId().equals(userId)) {
                continue; // Bỏ qua chính người dùng hiện tại
            }

            // Kiểm tra xem có bất kỳ mối quan hệ nào giữa hai người dùng không
            boolean hasRelation = friendshipRepository.existsByUserIdAndFriendId(userId, user.getId()) ||
                                friendshipRepository.existsByUserIdAndFriendId(user.getId(), userId);

            if (!hasRelation) {
                nonFriends.add(userService.convertToDTO(user));
            }
        }

        return nonFriends;
    }

    @Override
    public boolean unfriend(Long userId, Long friendId) {
        // Kiểm tra cả hai chiều của mối quan hệ bạn bè
        Optional<Friendship> friendship1 = friendshipRepository.findByUserIdAndFriendId(userId, friendId);
        Optional<Friendship> friendship2 = friendshipRepository.findByUserIdAndFriendId(friendId, userId);

        boolean success = false;

        // Xóa mối quan hệ nếu tồn tại và đã được chấp nhận
        if (friendship1.isPresent() && "ACCEPTED".equals(friendship1.get().getStatus())) {
            friendshipRepository.delete(friendship1.get());
            success = true;
        }

        if (friendship2.isPresent() && "ACCEPTED".equals(friendship2.get().getStatus())) {
            friendshipRepository.delete(friendship2.get());
            success = true;
        }

        return success;
    }
}