package com.example.SocketServer.Controller;

import com.example.SocketServer.Service.ChatService;
import com.example.SocketServer.TCPServer;
import com.example.SocketServer.User.Chat;
import com.example.SocketServer.User.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/chat")
public class ChatController {
    private final TCPServer tcpServer;
    // Constructor-based injection
    public ChatController(TCPServer tcpServer) {
        this.tcpServer = tcpServer;
    }

    @Autowired
    private ChatService chatService;

    @GetMapping("")
    public String chat(Model model) {
        return "chat";
    }
}
