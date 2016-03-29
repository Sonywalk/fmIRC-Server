package com.company;

import com.company.asciiart.AsciiGenerator;
import com.company.asciiart.AsciiLogo;
import com.company.database.DatabaseConnection;
import com.company.database.ModeratorDAO;

import java.io.IOException;
import java.util.Scanner;
import java.util.regex.Pattern;

public class InputHelper {
    private ConnectedClient client;
    private ModeratorDAO moderatorDAO;
    public InputHelper(ConnectedClient client) throws IOException {
        this.client = client;
        moderatorDAO = new DatabaseConnection();
    }

    public void processInput(String input) throws IOException {
        System.out.println(input);
        //If a client is connected (has has a valid nickname) he can do other requests such as messaging
        if (client.isConnected()) {
            if (input.startsWith("MSG")) {  //"MSG [to] :[message]" from client
                messageRequest(input);
            }
            else if(input.startsWith("ERROR")){
                error(input);
            }
            else if (input.startsWith("GET")) {
                getFile(input);
            }
            else if (input.startsWith("WHOIS")) {
                whois(input);
            }
            else if (input.startsWith("KICK")) {
                kick(input);
            }
            else if (input.startsWith("SENDING")) {
                sendingFile(input);
            }
            else if (input.startsWith("LISTBACK")) {
                listback(input);
            }
            else if (input.startsWith("LIST")) {
                list(input);
            }
            else if (input.startsWith("JOIN")) {
                join(input);
            }
            else if (input.startsWith("QUIT")) {
                quit(input);
            }
            else if(input.startsWith(("HELP"))){
                help();
            }
            else {
                notRecognized(input);
            }
        }
        else {
            if (input.startsWith("NICK")) {
                nicknameRequest(input);
            }
            else {
                client.write("> " + input);
                client.write("< you need a nickname, command: NICK [your_nickname]");
            }
        }
    }

    private void notRecognized(String input) throws IOException {
        client.write("> " + input);
        client.write("< Command not recognized");
    }


    private void help() throws IOException{
        client.write("> HELP");
        client.write("< Available commands: ");
        client.write("< Join channel: JOIN #{channelname} ");
        client.write("< Get information about user: WHOIS {nickname} ");
        client.write("< List available files from user: LIST {nickname}");
        client.write("< Get file from user: GET {nickname} :{filename}");
        client.write("< Put files for sharing in \"shared\" directory");
        client.write("");
        client.write("< If moderator: ");
        client.write("< Kick user from channel: KICK {nickname} #{channelname}");

    }

    private void kick(String input) throws IOException { //KICK [nickname] [channel]

        client.write("> " + input);
        String[] parts = input.replace("KICK ", "").split(" ");
        String nickname = parts[0];
        String channelIn = parts[1];

        Channel channel = ServerConnection.getChannel(channelIn);
        if (channel == null) {
            client.write("< Channel does not exist!");
            return;
        }

        //Returns null if moderator for channel with nickname not exists
        if (moderatorDAO.fetchModerator(client.getNickname(), channel.getId()) != null) {
            ConnectedClient clientToBeKicked = ServerConnection.getClient(nickname);
            if (clientToBeKicked == null) {
                client.write("< No user with that nickname found!");
                return;
            }

            clientToBeKicked.removeJoinedChannel(channel.getId());
            channel.removeFromOnlineList(clientToBeKicked);
            ServerConnection.channelMessage(channel.getId(), "QUIT " + channel.getId() + " " + nickname);
            clientToBeKicked.write("< You have been kicked from " + channel.getId());
        }
        else {
            client.write("< You are not a moderator!");
        }
    }

    private void whois(String input) throws IOException {
        client.write("> " + input);
        String target = input.replace("WHOIS ", "");
        String socketAddr = ServerConnection.getClient(target).getRemoteAddress().toString();
        String ip = socketAddr.substring(1, socketAddr.indexOf(":"));
        StringBuilder response = WhoisHttpRequest.execute(ip);
        if (response == null) {
            client.write("< Could not get whois data");
            return;
        }
        Scanner scan = new Scanner(response.toString());
        while (scan.hasNextLine() ){
            String line = scan.nextLine();
            client.write("< " + line);
        }
    }

    private void quit(String input) throws IOException {
        String[] parts = input.split(" "); //QUIT [channel] [nickname]
        String channelId = parts[1];
        String nickname = parts[2];
        Channel channel = ServerConnection.getChannel(channelId);
        channel.removeFromOnlineList(ServerConnection.getClient(nickname));
        client.removeJoinedChannel(channel.getId());
        if (channel.getOnlineList().isEmpty()) {
            ServerConnection.removeChannel(channel.getId());
        }
        for (ConnectedClient c : channel.getOnlineList()) {
            c.write("QUIT " + channel.getId() + " " + nickname);
        }
    }
    private void join(String input) {
        try {
            String channelId = input.replace("JOIN ", "");
            if (!channelId.startsWith("#")) {
                client.write("< channel name must start with '#'");
                return;
            }
            client.write("> " + input);

            //Returns the channel if it already exists
            Channel channel = ServerConnection.addChannel(channelId);

            if (client.getJoinedChannels().contains(channel.getId())) {
                client.write("< You have already joined " + channel.getId());
                return;
            }
            client.addJoinedChannel(channel.getId());
            for (ConnectedClient c : channel.getOnlineList()) {
                c.write("JOINED " + channel.getId() + " " +  client.getNickname());
            }
            channel.addToOnlineList(client);
            for (ConnectedClient c : channel.getOnlineList()) {
                client.write("JOINED " + channel.getId() + " " + c.getNickname());
            }
            client.write("< You joined " + channel.getId());

            StringBuilder ascii = AsciiGenerator.getAscii(channel.getId());
            Scanner scan = new Scanner(ascii.toString());
            while (scan.hasNextLine()) {
                String line = scan.nextLine();
                client.write("MSG " + client.getNickname() + "@" + channel.getId() + " :" + line);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void messageRequest(String input) throws IOException {
        try {
            int index = input.indexOf(":");
            String to = input.substring(0, index).replace("MSG", "").trim();
            String message = input.substring(index + 1, input.length());
            String output = "MSG " + client.getNickname() + "@" + to + " :" + message; //respond to clients with: "MSG [from]@[to] :[message]
            if (to.startsWith("#")) {
                ServerConnection.channelMessage(to, output);
            }
            else {
                ServerConnection.privateMessage(to, output);
                client.write(output);
            }
        }
        catch(Exception e) {
            client.write("< Invalid syntax for MSG");
        }
    }

    private void nicknameRequest(String input) throws IOException {
        String nick = input.replace("NICK ", "");
        if (Pattern.compile("[+/\\#$<>@:\\s]+").matcher(nick).find() || nick.length() < 3) { //if nick contains any of the chars it will return true
            client.write("NICK TAKEN");
            return;
        }
        if (ServerConnection.addClient(client, nick)) {
            client.setNickname(nick);
            client.setIsConnected(true); //The client should not be considered connected until he has a nickname
            client.write("NICK OK");
            client.write(AsciiLogo.ASCII_DEFAULT);
        }
        else {
            client.write("NICK TAKEN");
        }
    }

    private void error(String input) throws IOException{
        int index = input.indexOf(":");
        String error = input.substring(0, index - 1).replace("ERROR ", "");
        String receiver = input.substring(index + 1, input.length());
        ConnectedClient c = ServerConnection.getClient(receiver);
        client.write("< " + error);
        c.write("< " + error);
    }

    private void getFile(String input) {
        try {
            client.write("< " + input);
            int index = input.indexOf(":");
            String filename = input.substring(index + 1, input.length());
            String to = input.substring(0, index).replace("GET", "").trim();

            if(index == -1) {
                client.write("< Invalid command. Use command HELP for syntax for GET.");
                return;
            }
            if(filename.length() == 0){
                client.write("< Filename is empty.");
                return;
            }
            if(ServerConnection.clientTransferring(client)){
                client.write("< Transfer in progress, Please wait.");
                return;
            }
            if(ServerConnection.getClient(to) == null){
                client.write("< Nickname does not exist");
                return;
            }
            if(ServerConnection.clientTransferring(ServerConnection.getClient(to))){
                client.write("< Transfer in progress, Please wait.");
                return;
            }

            int port = 49152 + (int)(Math.random() * ((65535 - 49152) + 1));
            //Pass the two clients to transfer (used for remove them from inProgress when the job is done)
            new FileTransfer(client, ServerConnection.getClient(to), port).execute();
            ServerConnection.privateMessage(to, "GET " + client.getNickname() + " :" + filename + " [" + port + "]");
        }
        catch (Exception e) {
            try {
                client.write("< Incorrect syntax for command GET");
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }
    private void sendingFile(String input) throws IOException {
        int index = input.indexOf(":");
        String filename = input.substring(index+1, input.indexOf("/")).trim();
        String size = input.substring(input.indexOf("/") + 1, input.indexOf("[") - 1);
        String to = input.substring(0, index).replace("SENDING", "").trim();
        String port = input.substring(input.indexOf("[") + 1, input.indexOf("]"));
        ServerConnection.privateMessage(to, "SENDING :" + filename + " /" + size + " [" + port + "]");
        ServerConnection.addTransferringClient(client);
    }
    private void list(String input) throws IOException {
        client.write("> " + input);

        String to = input.replace("LIST", "").trim();
        if(to.length() <= 0 || ServerConnection.getClient(to) == null){
            client.write("< Nickname \"" + to + "\" does not exist.");
            return;
        }
        ServerConnection.privateMessage(to, "LIST " + client.getNickname());
    }
    private void listback(String input) {
        try {
            int index = input.indexOf(":");
            String msg = input.substring(index+1, input.length());
            String to = input.substring(0, index-1).replace("LISTBACK", "").trim();
            ServerConnection.getClient(to).write("< " + msg);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
