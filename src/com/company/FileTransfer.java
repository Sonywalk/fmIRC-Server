package com.company;

import javax.swing.*;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

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
        try {
            ServerSocket socket = new ServerSocket(port);

            while(receiver == null || sender == null) {
                if (receiver == null && sender == null) {
                    sender = socket.accept();
                    bin = new BufferedInputStream(sender.getInputStream());
                }
                if (sender != null && receiver == null) {
                    receiver = socket.accept();
                    bout = new BufferedOutputStream(receiver.getOutputStream());
                }
            }
            byte[] buff = new byte[BUFF_SIZE];
            int len;
            while ((len = bin.read(buff)) != -1) {
                bout.write(buff, 0, len);
                System.out.println(len);
            }
            bout.flush();
            bout.close();
            bin.close();
            socket.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void done(){
        System.out.println("Transfer is done");
        ServerConnection.removeTransferringClient(connectedClient1);
        ServerConnection.removeTransferringClient(connectedClient2);
    }
}