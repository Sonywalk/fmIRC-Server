package com.company;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by LanfeaR on 2016-02-07.
 */
public class ConnectedClient implements Runnable {
    private Socket clientSocket;
    private BufferedReader in;
    private BufferedWriter out;
    private String nickname;
    private boolean isConnected;
    private ArrayList<String> joinedChannels;

    public ConnectedClient(Socket clientSocket) throws IOException {
        this.clientSocket = clientSocket;
        joinedChannels = new ArrayList<>();
        this.out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"));
        write("NICK?");
    }

    @Override
    public void run() {
        String line;
        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
            InputHelper helper = new InputHelper(this);
            while ((line = in.readLine()) != null) {
                helper.processInput(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void write(String msg) throws IOException {
        out.write(msg + "\r\n");
        out.flush();
    }

    private void disconnect() throws IOException {
        //Write to all clients in all channels that this client was joined into
        //Check if channels is empty if it is remove it
        ServerConnection.removeClient(this.nickname);
        for (String item : this.joinedChannels) {
            ServerConnection.getChannel(item).removeFromOnlineList(this);
            if (ServerConnection.getChannel(item).getOnlineList().isEmpty()) {
                ServerConnection.removeChannel(item);
            }
            for (ConnectedClient c : ServerConnection.getChannel(item).getOnlineList()) {
                c.write("QUIT " + item + " " + this.nickname);
            }
        }
        clientSocket.close();
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    public String getNickname() {
        return this.nickname;
    }
    public boolean isConnected() {
        return this.isConnected;
    }
    public void setIsConnected(boolean isConnected) {
        this.isConnected = isConnected;
    }
    public void removeJoinedChannel(String channelId) {
        joinedChannels.remove(channelId);
    }
    public void addJoinedChannel(String channelId) {
        joinedChannels.add(channelId);
    }
    public ArrayList<String> getJoinedChannels() {
        return this.joinedChannels;
    }
}
