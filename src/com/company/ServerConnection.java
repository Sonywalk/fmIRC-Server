package com.company;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by LanfeaR on 2016-02-07.
 */
public class ServerConnection {
    private final static int PORT = 1337;
    private final static int NUMBER_OF_CLIENTS_ALLOWED = 20;
    public static HashMap<String, Channel> channels;
    public static HashMap<String, ConnectedClient> clients;
    public static ServerSocket serverSocket;
    private static List<ConnectedClient> transferInProgress;

    public ServerConnection() {
        clients = new HashMap<>();
        channels = new HashMap<>();
        transferInProgress = new ArrayList<>();
        startServer();
    }

    private void startServer() {
        final ExecutorService clientProcessingPool = Executors.newFixedThreadPool(NUMBER_OF_CLIENTS_ALLOWED);
        Runnable serverTask = () -> {
            try {
                serverSocket = new ServerSocket(PORT);
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

    public static InetAddress getInetAddress() {
        return serverSocket.getInetAddress();
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

    public static void removeTransferringClient(ConnectedClient client){
        transferInProgress.remove(client);
    }
    public static void addTransferringClient(ConnectedClient client){
        transferInProgress.add(client);
    }

    public static boolean clientTransferring(ConnectedClient client){
        return transferInProgress.contains(client);
    }

    public static void removeClient(String nick) {
        clients.remove(nick);
    }

    public static ConnectedClient getClient(String key) {
        return clients.get(key);
    }

    public static void privateMessage(String to, String msg) throws IOException {
        clients.get(to).write(msg);
    }

    public static void channelMessage(String to, String msg) throws IOException {
        for (ConnectedClient c : channels.get(to).getOnlineList()) {
            c.write(msg);
        }
    }

    //Returns the channel if already exists else it creates the channel and returns it
    public static Channel addChannel(String id) {
        for (String key : channels.keySet()) {
            if (key.equals(id)) {
                return channels.get(key);
            }
        }
        Channel c = new Channel(id);
        channels.put(id, c);
        return c;
    }
    public static Channel getChannel(String key) {
        return channels.get(key);
    }

    public static void removeChannel(String key) {
        channels.remove(key);
    }
}
