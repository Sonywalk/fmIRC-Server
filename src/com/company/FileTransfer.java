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

    @Override
    protected Void doInBackground() throws Exception {
        ServerSocket socket = new ServerSocket(PORT);

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
            try {
                bout.write(buff, 0, len);
            }
            catch (Exception e) {
                e.printStackTrace();
            }

        }
        bout.flush();
        bout.close();
        bin.close();
        socket.close();
        return null;
    }
}