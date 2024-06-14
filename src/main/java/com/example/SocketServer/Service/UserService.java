package com.example.SocketServer.Service;

import com.example.SocketServer.Repo.UserRepository;
import com.example.SocketServer.User.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User findByNameAndPassword(String name, String password) {
        return userRepository.findByNameAndPassword(name, password);
    }
}