package com.example.SocketServer;

import com.example.SocketServer.Service.ChatService;
import com.example.SocketServer.User.Chat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import java.util.concurrent.CopyOnWriteArrayList;

import java.io.IOException;

public class WebSocketHandler extends TextWebSocketHandler {

    private CopyOnWriteArrayList<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    private TCPServer tcpServer;
    // Setter for TCPServer
    public void setTcpServer(TCPServer tcpServer) {
        this.tcpServer = tcpServer;
    }

    @Autowired
    private ChatService chatService;

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws IOException {
        // Handle incoming messages here
        String receivedMessage = (String) message.getPayload();
        System.out.println(receivedMessage);
        // Process the message and send a response if needed
        if(receivedMessage.equals("CHECK_LEADER_CONNECTION")){
            tcpServer.checkLeaderConnection();
        }else if(receivedMessage.equals("SHUTDOWN_SERVER")) {
            tcpServer.shutdown();
        }else if(receivedMessage.equals("STARTING_SERVER")) {
            tcpServer.restart();
        }else if(receivedMessage.startsWith("SEND: ")){
            tcpServer.broadcastMessage(receivedMessage);
            String part[] = receivedMessage.split(": ");
            String parts[] = part[1].split("#");
            sendMessageToClient("MESSAGE: "+parts[0] + "#" + parts[1], session);
            if(tcpServer.getID() == tcpServer.getCurrentLeaderID()){
                chatService.saveChat(new Chat(parts[1], parts[0], tcpServer.getID() + ""));
            }
        }else{
            int currentLeaderID = tcpServer.getCurrentLeaderID();
            session.sendMessage(new TextMessage("Current Leader ID: " + currentLeaderID));
        }
    }
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {
        sessions.add(session);
        int currentLeaderID = tcpServer.getCurrentLeaderID();
        session.sendMessage(new TextMessage("SERVER ID: " + tcpServer.getID()));
        session.sendMessage(new TextMessage("Current Leader ID: " + currentLeaderID));
    }
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
    }

    public void sendMessageToClient(String message) {
        System.out.println(message);
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(message));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void sendMessageToClient(String message, WebSocketSession excludedSession) {
        System.out.println(message);
        for (WebSocketSession session : sessions) {
            if (session.isOpen() && !session.equals(excludedSession)) {
                try {
                    session.sendMessage(new TextMessage(message));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}