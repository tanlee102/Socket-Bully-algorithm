// TCPServer.java
package com.example.SocketServer;

import com.example.SocketServer.Service.ChatService;
import com.example.SocketServer.Service.UserService;
import com.example.SocketServer.User.Chat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

@Component
public class TCPServer implements CommandLineRunner {

    @Autowired
    private ChatService chatService;

    private boolean running = true;
    private ServerSocket serverSocket = null;
    private ExecutorService threadPool = Executors.newCachedThreadPool();
    private Map<String, ServerConnection> serverConnections = new ConcurrentHashMap<>();

    private int port;

    private int ID = 0;
    private int currentLeaderID = 0;

    private List<Socket> clientSockets = new CopyOnWriteArrayList<>();

    private WebSocketHandler webSocketHandler;
    // Setter for WebSocketHandler
    public void setWebSocketHandler(WebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }

    @Override
    public void run(String... args) throws Exception {
        starting();
    }

    private void starting(){
        try {
            // Load server configuration
            Properties config = loadConfig();
            port = Integer.parseInt(config.getProperty("port"));
            ID = Integer.parseInt(config.getProperty("ID"));
            currentLeaderID = ID;

            serverSocket = new ServerSocket(port);
            LogToClient("Self assigned this server ID:"+ID+" as leader and listening on port:" + port);

            // Load other server addresses
            String[] otherServers = config.getProperty("servers").split(",");
            for (String serverInfo : otherServers) {
                String[] parts = serverInfo.split("@");
                String[] parts_ = parts[1].split(":");

                int IDNumber = Integer.parseInt(parts[0]);
                String host = parts_[0];
                int portNumber = Integer.parseInt(parts_[1]);

                serverConnections.put(serverInfo, new ServerConnection(IDNumber, host, portNumber, threadPool, this));
            }

            // Start the connection retry mechanism
            startConnection();

            // Start leader election process
            startElection();

            // Start thread to accept incoming connections
            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    clientSockets.add(clientSocket);

                    InetSocketAddress clientAddress = (InetSocketAddress) clientSocket.getRemoteSocketAddress();
                    String clientIP = clientAddress.getAddress().getHostAddress();
                    int clientPort = clientAddress.getPort();
                    String clientFullAddress = clientIP + ":" + clientPort;
                    LogToClient("New connected from: " + clientFullAddress);

                    threadPool.submit(new ClientHandler(clientSocket, this));
                } catch (IOException e) {
                    if (!running) {
                        break; // Exit loop if shutting down
                    }
                    e.printStackTrace();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            shutdown();
        }
    }

    public void shutdown() {
        try {
            running = false;
            if (serverSocket != null) {
                serverSocket.close();
            }
            for (ServerConnection connection : serverConnections.values()) {
                connection.close();
            }
            serverSocket = null;
            threadPool.shutdown();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void restart() {
        LogToClient("Socket restarting...");
        shutdown();  // Ensure existing resources are cleaned up
        running = true;  // Reset running flag
        threadPool = Executors.newCachedThreadPool();  // Reinitialize the thread pool
        starting();  // Restart the server
    }

    private Properties loadConfig() throws IOException {
        Properties config = new Properties();
        try (InputStream input = new FileInputStream("config.properties")) {
            config.load(input);
        }
        return config;
    }

    public int getID() {
        return ID;
    }

    public int getCurrentLeaderID() {
        return currentLeaderID;
    }

    private void startConnection() {
        threadPool.submit(() -> {
            for (ServerConnection connection : serverConnections.values()) {
                connection.connect();
            }
        });
    }

    public void startElection() {
        threadPool.submit(() -> {
            try {
                Thread.sleep(10000); // Give some time for initial connections
                LogToClient("Self starting new election...");
                boolean higherIDResponded = false;
                for (ServerConnection connection : serverConnections.values()) {
                    if (ID < connection.getID()) {
                        String response = connection.sendAndReceive("ELECTION");
                        if (response != null && response.equals("OK")) {
                            higherIDResponded = true;
                            break; // Exit the loop if "OK" response received
                        }
                    }
                }
                if (!higherIDResponded) {
                    declareAsLeader();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }


    private void declareAsLeader() throws IOException {
        currentLeaderID = ID;
        LogToClient("This server ID:"+currentLeaderID+" is now the leader");
        LogToClient("Current Leader ID: " + currentLeaderID);
        for (ServerConnection connection : serverConnections.values()) {
            connection.sendMessage("COORDINATOR:" + currentLeaderID);
        }
    }

    public void broadcastMessage(String message) throws IOException {
        for (ServerConnection connection : serverConnections.values()) {
            connection.sendMessage(message);
        }
    }


    public void checkLeaderConnection() throws IOException {
        if (currentLeaderID != ID){
            if(!getServerConnection(currentLeaderID).checkConnection()){
                LogToClient("Leader with ID:"+currentLeaderID+" lost connection...Starting new election...");
                startElection();
            }
        }
    }

    private class ClientHandler implements Runnable {
        private Socket clientSocket;
        private TCPServer tcpServer;

        public ClientHandler(Socket socket, TCPServer tcpServer) {
            this.clientSocket = socket;
            this.tcpServer = tcpServer;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
                String message;
                while ((message = in.readLine()) != null) {
                    if(message.equals("ping")){
                        out.println("pong");
                    }else if (message.equals("ELECTION")) {
                        out.println("OK");
                        LogToClient("Starting new election...");
                        tcpServer.startElection();
                    } else if (message.startsWith("COORDINATOR")) {
                        int newLeaderID = Integer.parseInt(message.split(":")[1]);
                        currentLeaderID = newLeaderID;
                        LogToClient("New leader is server with ID:" + newLeaderID);
                        LogToClient("Current Leader ID: " + newLeaderID);
                    }else if(message.startsWith("CHECK_LEADER_CONNECTION")){
                        checkLeaderConnection();
                    }else if(message.startsWith("SEND: ")){
                        String part[] = message.split(": ");
                        String parts[] = part[1].split("#");
                        LogToClient("MESSAGE: "+parts[0] + "#" + parts[1]);
                        if(tcpServer.getID() == tcpServer.getCurrentLeaderID()){
                            chatService.saveChat(new Chat(parts[1], parts[0], tcpServer.getID() + ""));
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (clientSocket != null) clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public ServerConnection getServerConnection(int _ID) {
        for (ServerConnection connection : serverConnections.values()) {
            if (connection.getID() == _ID) {
                return connection;
            }
        }
        return null;
    }

    public void LogToClient(String message){
        webSocketHandler.sendMessageToClient(message);
    }
}