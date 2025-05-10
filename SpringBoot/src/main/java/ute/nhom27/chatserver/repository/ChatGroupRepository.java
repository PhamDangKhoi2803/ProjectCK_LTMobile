package ute.nhom27.chatserver.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ute.nhom27.chatserver.entity.ChatGroup;

@Repository
public interface ChatGroupRepository extends JpaRepository<ChatGroup, Long> {
}
