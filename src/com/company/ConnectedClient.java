package com.company;

import java.io.*;
import java.net.Socket;

/**
 * Created by LanfeaR on 2016-02-07.
 */
public class ConnectedClient implements Runnable {
    private Socket clientSocket;
    private BufferedReader in;
    private BufferedWriter out;
    private String nickname;
    private boolean isConnected;

    public ConnectedClient(Socket clientSocket) throws IOException {
        nickname = "";
        this.clientSocket = clientSocket;
        this.out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"));
        write("NICK?");
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        String line;
        try {
            InputHelper helper = new InputHelper(this);
            while ((line = in.readLine()) != null) {
                helper.processInput(line);
            }
            disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void write(String msg) throws IOException {
        out.write(msg + "\r\n");
        out.flush();
    }

    private void disconnect() throws IOException {
        ServerConnection.removeClient(getNickname());
        clientSocket.close();
        if (isConnected()) {
            ServerConnection.broadcastMessage("QUIT " + getNickname());
        }
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    public String getNickname() {
        return this.nickname;
    }
    public boolean isConnected() {
        return isConnected;
    }
    public void setIsConnected(boolean isConnected) {
        this.isConnected = isConnected;
    }
}
