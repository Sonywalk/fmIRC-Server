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
    private final static int PORT = 1338;
    private Socket receiver = null;
    private Socket sender = null;
    private final static int BUFF_SIZE = 8*1024;
    private ConnectedClient connectedClient1 = null;
    private ConnectedClient connectedClient2 = null;

    public FileTransfer(ConnectedClient client1, ConnectedClient client2){
        this.connectedClient1 = client1;
        this.connectedClient2 = client2;

    }

    @Override
    protected Void doInBackground() throws Exception {
        ServerSocket socket = new ServerSocket(PORT);

        while(receiver == null || sender == null) {
            if (receiver == null && sender == null) {
                sender = socket.accept();
                bin = new BufferedInputStream(sender.getInputStream());
                System.out.println("sender ok");
            }
            if (sender != null && receiver == null) {
                receiver = socket.accept();
                bout = new BufferedOutputStream(receiver.getOutputStream());
                System.out.println("Receiver ok");
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
        return null;
    }

    @Override
    protected void done(){
        ServerConnection.removeTransferringClient(connectedClient1);
        ServerConnection.removeTransferringClient(connectedClient2);
    }
}