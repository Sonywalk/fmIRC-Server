package com.company;

import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Created by LanfeaR on 2016-02-07.
 */
public class InputHelper {
    private ConnectedClient client;

    public InputHelper(ConnectedClient client) throws IOException {
        this.client = client;
    }

    public void processInput(String input) throws IOException {
        //If a client is connected (has has a valid nickname) he can do other requests such as messaging
        if (client.isConnected()) {
            if (input.startsWith("PRIVMSG")) {  //"PRIVMSG [to] :[message]" from client
                privateMessageRequest(input);
            }
            else {
                ServerConnection.broadcastMessage("MESSAGE " + client.getNickname() + ":" + input);
            }
        }
        else {
            if (input.startsWith("NICK")) {
                nicknameRequest(input);
            }
            else {
                client.write("ERROR you need a nickname, command: NICK [your_nickname]");
            }
        }
    }

    private void privateMessageRequest(String input) throws IOException {
        int index = input.indexOf(":");
        String to = input.substring(0, index).replace("PRIVMSG", "").trim();
        String message = input.substring(index + 1, input.length());
        String output = "PRIVMSG " + client.getNickname() + "@" + to + " :" + message; //respond to clients with: "PRIVMSG [from]@[to] :[message]
        ServerConnection.privateMessage(to, output);
        client.write(output);
    }

    private void nicknameRequest(String input) throws IOException {
        String nick = input.replace("NICK ", "");
        if (Pattern.compile("[+/\\@:\\s]+").matcher(nick).find() || nick.length() < 3) { //if nick contains any of the chars it will return true
            client.write("NICK TAKEN");
            return;
        }
        if (ServerConnection.addClient(client, nick)) {
            System.out.println("Added client " + nick);
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
