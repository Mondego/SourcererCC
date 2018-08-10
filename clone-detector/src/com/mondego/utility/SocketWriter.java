package com.mondego.utility;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mondego.indexbased.SearchManager;

public class SocketWriter {

    private Socket socket;
    private OutputStream os;
    // public PrintWriter pwrite;
    public BufferedWriter pwrite;
    private int port;
    private String address;
    private static final Logger logger = LogManager
            .getLogger(SocketWriter.class);

    public SocketWriter(int port, String address) {
        this.port = port;
        this.address = address;

    }

    public void openSocketForWriting() {
        try {
            // serverSocket = new ServerSocket(port);
            System.out.println("establishing connection..");
            socket = new Socket(this.address, this.port);
            System.out.println("connected.");
            os = socket.getOutputStream();
            pwrite = new BufferedWriter(
                    new OutputStreamWriter(os, StandardCharsets.UTF_8),
                    SearchManager.properties.getInt("SOCKET_BUFFER"));
            // pwrite = new PrintWriter(new
            // OutputStreamWriter(os,StandardCharsets.UTF_8), false);

        } catch (Exception e) {
            System.out.println("EXITING" + e.getMessage());
            System.exit(1);
        }
    }

    public synchronized void writeToSocket(String msg) {
        try {
            this.pwrite.write(msg + System.lineSeparator());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeSocket() {
        try {
            this.writeToSocket("FINISHED_JOB" + System.lineSeparator());
            this.pwrite.flush();
            this.socket.close();
        } catch (Exception e) {
            System.out.println("error closing socket" + e.getMessage());
        }
    }
    /*
     * public static void main(String args[]){ SocketWriter s = new
     * SocketWriter(9999,"localhost"); s.openSocketForWriting();
     * s.writeToSocket("hi this is java here");
     * s.writeToSocket("having a good day"); s.writeToSocket("msg 3");
     * s.writeToSocket("FINISHED_JOB"); }
     */
}
