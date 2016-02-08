package com.company;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by LanfeaR on 2016-02-07.
 */
public class ServerConnection {
    private final static int PORT = 1337;
    private final static int NUMBER_OF_CLIENTS_ALLOWED = 20;

    public static HashMap<String, ConnectedClient> clients;

    public ServerConnection() {
        clients = new HashMap<>();
        startServer();
    }

    private void startServer() {
        final ExecutorService clientProcessingPool = Executors.newFixedThreadPool(NUMBER_OF_CLIENTS_ALLOWED);
        Runnable serverTask = () -> {
            try {
                ServerSocket serverSocket = new ServerSocket(PORT);
                System.out.println("Waiting for clients to connect...");
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    ConnectedClient c = new ConnectedClient(clientSocket);
                    clientProcessingPool.submit(c);
                }
            } catch (IOException e) {
                System.out.println("Unable to process client request");
                e.printStackTrace();
            }
        };
        Thread serverThread = new Thread(serverTask);
        serverThread.start();
    }

    public static void broadcastMessage(String message) throws IOException {
        for (ConnectedClient c : clients.values()) {
            c.write(message);
        }
    }

    public static boolean addClient(ConnectedClient c, String nick) {
        for (String key : clients.keySet()) {
            if (key.equals(nick)) {
                return false;
            }
        }
        clients.put(nick, c);
        return true;
    }

    public static void removeClient(String nick) {
        clients.remove(nick);
    }
}
