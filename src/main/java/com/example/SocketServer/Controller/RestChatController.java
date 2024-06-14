package com.example.SocketServer.Controller;

import com.example.SocketServer.Service.ChatService;
import com.example.SocketServer.User.Chat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/mychat")
public class RestChatController {

    @Autowired
    private ChatService chatService;

    @GetMapping
    public List<Chat> getchat(Model model) {
        return chatService.getAllChats();
    }
}
