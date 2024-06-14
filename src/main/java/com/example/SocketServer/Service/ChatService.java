package com.example.SocketServer.Service;

import com.example.SocketServer.Repo.ChatRepository;
import com.example.SocketServer.Repo.UserRepository;
import com.example.SocketServer.User.Chat;
import com.example.SocketServer.User.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatService {

    @Autowired
    private ChatRepository chatRepository;

    public Chat saveChat(Chat chat) {
        return chatRepository.save(chat);
    }

    public List<Chat> getAllChats() {
        return chatRepository.findAll();
    }
}