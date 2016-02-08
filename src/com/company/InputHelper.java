package com.company;

import java.io.IOException;

/**
 * Created by LanfeaR on 2016-02-07.
 */
public class InputHelper {
    private ConnectedClient client;

    public InputHelper(ConnectedClient client) throws IOException {
        this.client = client;
    }

    public void processInput(String input) throws IOException {
        if (client.getNickname() == null) {
            client.write("NICK?");
        }
        else if (input.startsWith("NICK")) {
            nicknameRequest(input.replace("NICK", "").trim());
        }
        else {
            ServerConnection.broadcastMessage("MESSAGE " + client.getNickname() + ":" + input);
        }
    }

    private void nicknameRequest(String nick) throws IOException {
        if (ServerConnection.addClient(client, nick)) {
            client.setNickname(nick);
            client.setIsConnected(true); //The client should not be considered connected until he has a nickname
            client.write("NICK OK");
            for (String s : ServerConnection.clients.keySet()) {
                if (!s.equals(nick)) {
                    client.write("JOINED " + s);
                }
            }
            ServerConnection.broadcastMessage("JOINED " + nick);
        }
        else {
            client.write("NICK TAKEN");
        }
    }
}
