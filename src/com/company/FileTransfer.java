package com.company;

import javax.swing.*;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * Created by LanfeaR on 2016-02-11.
 */
public class FileTransfer extends SwingWorker<Void, Void> {
    private BufferedInputStream bin;
    private BufferedOutputStream bout;
    private Socket receiver = null;
    private Socket sender = null;
    private final static int BUFF_SIZE = 8*1024;
    private ConnectedClient connectedClient1 = null;
    private ConnectedClient connectedClient2 = null;
    private int port;

    public FileTransfer(ConnectedClient client1, ConnectedClient client2, int port){
        this.connectedClient1 = client1;
        this.connectedClient2 = client2;
        this.port = port;
    }

    @Override
    protected Void doInBackground() throws Exception {
        ServerSocket socket = null;
        try {
            socket = new ServerSocket(port);
            socket.setSoTimeout(5000);

            sender = socket.accept();
            bin = new BufferedInputStream(sender.getInputStream());
            receiver = socket.accept();
            bout = new BufferedOutputStream(receiver.getOutputStream());

            System.out.println("Ready to transfer");

            byte[] buff = new byte[BUFF_SIZE];
            int len;
            while ((len = bin.read(buff)) != -1) {
                bout.write(buff, 0, len);
                System.out.println(len);
            }
        }
        catch (SocketTimeoutException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (bout != null) {
                bout.flush();
                bout.close();
            }
            if (receiver != null) {
                receiver.close();
            }
            if (bin != null) {
                bin.close();
            }
            if (sender != null) {
                sender.close();
            }
            if (socket != null) {
                socket.close();
            }
            ServerConnection.removeTransferringClient(connectedClient1);
            ServerConnection.removeTransferringClient(connectedClient2);
            System.out.println("Transfer is done");
        }
        return null;
    }
}