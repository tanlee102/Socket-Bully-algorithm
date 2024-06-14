function sleep(ms) {
  return new Promise(resolve => setTimeout(resolve, ms));
}
document.addEventListener("DOMContentLoaded", () => {
    const output = document.getElementById('output');
    const shutdownBtn = document.getElementById('shutdownBtn');
    const startBtn = document.getElementById('startBtn');
    const checkBtn = document.getElementById('checkBtn');

    let socket = null; // Declare the socket variable outside the event listeners

    const establishWebSocketConnection = (firstload = true) => {
        return new Promise((resolve, reject) => {
           const wsProtocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
            const wsURL = `${wsProtocol}//${window.location.host}/websocket`;
            console.log(wsURL)
            socket = new WebSocket(wsURL);

            socket.addEventListener('open', () => {
                console.log('WebSocket connection established');
                if(!firstload) socket.send('STARTING_SERVER'); // Send "STARTING_SERVER" after connection is established
                resolve();
            });

            socket.addEventListener('message', (event) => {
                const message = event.data;
                // Update the server and leader IDs if applicable
                if (message.startsWith('SERVER ID:')) {
                    const serverId = message.split(': ')[1];
                    document.getElementById('server-id').textContent = `SERVER ID: ${serverId}`;
                } else if (message.startsWith('Current Leader ID:')) {
                    const leaderId = message.split(': ')[1];
                    document.getElementById('leader-id').textContent = `Current Leader ID: ${leaderId}`;
                } else {
                    // Append to output only if it's not an update message
                    const line = document.createElement('div');
                    line.className = 'line';
                    line.textContent = message;
                    output.appendChild(line);
                    output.scrollTop = output.scrollHeight;  // Scroll to the bottom
                }
            });

            socket.addEventListener('close', () => {
                console.log('WebSocket connection closed');
            });

            socket.addEventListener('error', (error) => {
                console.error('WebSocket error:', error);
                reject(error);
            });
        });
    };

    shutdownBtn.addEventListener('click', async () => {
        if (socket && socket.readyState === WebSocket.OPEN) {
            socket.close(); // Close the existing connection
            await sleep(1000); // Sleep for 2000 milliseconds (2 seconds)
            await establishWebSocketConnection(true); // Create a new connection
            await sleep(1000); // Sleep for 2000 milliseconds (2 seconds)
            socket.send('SHUTDOWN_SERVER'); // Send "SHUTDOWN_SERVER" before closing the connection
            socket.close(); // Close the existing connection
            const line = document.createElement('div');
            line.className = 'line';
            line.textContent = "Shutdown.";
            output.appendChild(line);
        } else {
            await establishWebSocketConnection(true); // Create a new connection
            await sleep(1000); // Sleep for 2000 milliseconds (2 seconds)
            socket.send('SHUTDOWN_SERVER'); // Send "SHUTDOWN_SERVER" before closing the connection
            socket.close(); // Close the existing connection
            const line = document.createElement('div');
            line.className = 'line';
            line.textContent = "Shutdown.";
            output.appendChild(line);
        }
    });

    startBtn.addEventListener('click', async () => {
        if (socket && socket.readyState === WebSocket.OPEN) {
            socket.close(); // Close the existing connection
            await sleep(2000); // Sleep for 2000 milliseconds (2 seconds)
            await establishWebSocketConnection(false); // Create a new connection
        } else {
            await establishWebSocketConnection(false); // Create a new connection
        }
    });

    checkBtn.addEventListener('click', async () => {
            socket.close(); // Close the existing connection
            await establishWebSocketConnection(true); // Create a new connection
            await sleep(2000); // Sleep for 2000 milliseconds (2 seconds)
            socket.send('CHECK_LEADER_CONNECTION');
    });

    // Initial connection setup
    establishWebSocketConnection();
});
