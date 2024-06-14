// ServerConnection.java
package com.example.SocketServer;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

public class ServerConnection {
    private int ID;
    private String host;
    private int port;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private ExecutorService threadPool;
    private TCPServer tcpServer;

    public ServerConnection(int ID, String host, int port, ExecutorService threadPool, TCPServer tcpServer) {
        this.ID = ID;
        this.host = host;
        this.port = port;

        this.threadPool = threadPool;
        this.tcpServer = tcpServer;
    }

    public synchronized void connect() {
        threadPool.submit(() -> {
            try {
                socket = new Socket(host, port);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                tcpServer.LogToClient("Connected to server ID:" + ID);
                handleServerConnection();
            } catch (IOException e) {
                tcpServer.LogToClient("Failed to connect to server ID:" + ID);
                close();
            }
        });
    }
    private void handleServerConnection() {
        threadPool.submit(() -> {
            String message;
            try {
                while ((message = in.readLine()) != null) {
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                close();
            }
        });
    }
    public void sendMessage(String message) throws IOException {
        if(socket != null && socket.isConnected() && !socket.isClosed() && (out != null)) {
            out.println(message);
        }else{
            close();
            connect();
            sendAndReceive(message);
        }
    }
    public synchronized void close() {
        try {
            if (socket != null) {
                socket.close();
                socket = null;
                in = null;
                out = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }





    public boolean checkConnection() throws IOException {
        String response = sendAndReceive("ping");
        return response != null && !response.isEmpty();
    }

    public String sendAndReceive(String message) throws IOException {
        try (Socket socket = new Socket(host, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println(message);
            String response = in.readLine();
            return response;
        } catch (IOException e) {
            return null;
        }
    }

    public int getID(){
        return ID;
    }
    public String getHost() {
        return host;
    }
    public int getPort() {
        return port;
    }

}