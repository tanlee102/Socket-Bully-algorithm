package com.example.SocketServer.User;

import org.springframework.data.annotation.Id;

public class Chat {
    @Id
    private String id;
    private String name;
    private String text;
    private String hostId;

    public Chat(String name, String text, String hostId) {
        this.name = name;
        this.text = text;
        this.hostId = hostId;
    }

    public String getName() {
        return name;
    }

    public String getText() {
        return text;
    }

    public String getHostId() {
        return hostId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setHostId(String hostId) {
        this.hostId = hostId;
    }
}
