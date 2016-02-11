package com.company;

import javax.swing.*;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import com.google.common.io.ByteStreams;

/**
 * Created by LanfeaR on 2016-02-11.
 */
public class FileTransfer extends SwingWorker<Void, Void> {
    private BufferedInputStream bin;
    private BufferedOutputStream bout;
    private final static int PORT = 1338;
    private Socket receiver = null;
    private Socket sender = null;

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
        byte[] buff = new byte[8 * 1024];
        int len;
        while ((len = bin.read(buff)) != -1) {
            bout.write(buff, 0, len);
        }
        bout.flush();
        bout.close();
        bin.close();
        socket.close();
        return null;
    }
}