<img width="489" alt="image" src="https://github.com/user-attachments/assets/bb20e5e5-4c47-40f4-8381-86e27304c4ea">The project "Building a mutual exclusion application using the Bully algorithm" has successfully implemented a distributed system using Java and Spring Boot to manage and coordinate processes. The application includes a TCP server (TCPServer) and a WebSocket handler (WebSocketHandler) to manage connections and messages from clients.

The TCP server is responsible for listening to connections from clients, managing connections with other servers, and conducting leader elections using the Bully algorithm. When a leader loses connection, the server will restart the election process to ensure there is always a leading process to control activities in the system.

The WebSocket handler helps manage WebSocket connections from clients, handles received messages, and interacts with the TCP server to perform tasks such as checking leader connections, restarting or shutting down the server, and processing chat messages. All necessary messages are transmitted to all connected clients via WebSocket, ensuring the system's consistency and efficiency.

The system has been deployed and tested, ensuring stable and accurate operation in managing and coordinating distributed processes, meeting the requirements of mutual exclusion in a distributed environment.


Data:

<img width="489" alt="image" src="https://github.com/user-attachments/assets/f0319ad6-a71f-477b-9ecc-3369cbd0cb30">


Application Forms:

<img width="353" alt="image" src="https://github.com/user-attachments/assets/200be047-ce5c-49ca-82c8-6f28588a441d">

Shutdown: Disconnect the server.
Restart: Reconnect the server.
Check leader: Identify the leader with the largest ID.
Chat: Represents communication between servers.


Specific Functions:

Shutdown combined with check leader:

<img width="407" alt="image" src="https://github.com/user-attachments/assets/d730bf11-7f00-478f-8d1f-2f151d4fd4d0">

- Perform the shutdown on the server with ID 3 and check the leader on the server with ID 1.
The result shows that the new leader is the server with ID 2.


Restart combined with check leader:

<img width="400" alt="image" src="https://github.com/user-attachments/assets/8cc1ba73-9de1-4f42-92ff-e794c04e2f8a">

- Perform the restart function and start the leader election again.


Multi-server chatbot function:

<img width="383" alt="image" src="https://github.com/user-attachments/assets/6f145772-3cdd-4bd3-8d57-ea69699af9c0">
