package ute.nhom27.chatserver.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ute.nhom27.chatserver.entity.Friendship;
import ute.nhom27.chatserver.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {
    boolean existsByUserIdAndFriendId(Long userId, Long friendId);
    Optional<Friendship> findByUserIdAndFriendId(Long userId, Long friendId);
    @Query("SELECT DISTINCT f FROM Friendship f WHERE f.status = 'ACCEPTED' AND (f.user.id = :userId OR f.friend.id = :userId)")
    List<Friendship> findAcceptedFriendships(Long userId);
    List<Friendship> findByFriendIdAndStatus(Long friendId, String status);
    List<Friendship> findByUserIdAndStatus(Long userId, String status);
}