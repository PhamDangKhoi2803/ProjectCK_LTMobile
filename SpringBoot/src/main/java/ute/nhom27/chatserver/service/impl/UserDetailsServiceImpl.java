package ute.nhom27.chatserver.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ute.nhom27.chatserver.entity.User;
import ute.nhom27.chatserver.repository.UserRepository;

import java.util.ArrayList;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String phoneOrEmail) throws UsernameNotFoundException {
        User user = userRepository.findByPhoneOrEmail(phoneOrEmail, phoneOrEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with phone or email: " + phoneOrEmail));
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                new ArrayList<>() // Danh sách quyền, để trống vì chưa cần phân quyền
        );
    }

    public User getUserByPhoneOrEmail(String phoneOrEmail) {
        return userRepository.findByPhoneOrEmail(phoneOrEmail, phoneOrEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with phone or email: " + phoneOrEmail));
    }
}
