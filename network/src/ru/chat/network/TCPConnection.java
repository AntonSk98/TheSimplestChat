package ru.chat.network;

import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;

public class TCPConnection {
    private final Socket socket;
    private final Thread rxThread;
    private final BufferedReader in;
    private final TCPConnectionListener eventListener;
    private final BufferedWriter out;
    public TCPConnection(TCPConnectionListener eventListener, String ipAddres, int port) throws IOException{
        this(new Socket(ipAddres, port), eventListener);

    }

    public TCPConnection(Socket socket, TCPConnectionListener eventListener) throws IOException {
        this.eventListener=eventListener;
        this.socket=socket;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), Charset.forName("UTF-8")));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), Charset.forName("UTF-8")));
        rxThread = new Thread(new Runnable() { //anonymous class
            @Override
            public void run() {
                try {
                    eventListener.onConnectionReady(TCPConnection.this);
                    while (!rxThread.isInterrupted()){
                        String msg =in.readLine();
                        eventListener.onReceiveString(TCPConnection.this, msg);
                    }
                } catch (IOException e) {
                    eventListener.onException(TCPConnection.this, e);
                }finally {//below I will use universal way, according to what will work either server or client this block
                    //will react different, to realise it I would prefer to use interface
                    eventListener.onDisconnection(TCPConnection.this);

                }

            }
        });
        rxThread.start();
    }
    public synchronized void sendString(String value){
        try {
            out.write(value +"\r\n");
            out.flush();
        } catch (IOException e) {
            eventListener.onException(TCPConnection.this, e);
            disconnext();
        }
    }
    public synchronized void disconnext(){
        rxThread.interrupt();
        try {
            socket.close();
        } catch (IOException e) {
            eventListener.onException(TCPConnection.this, e);
        }

    }

    @Override
    public String toString() {
        return "ru.chat.network.TCPConnection "+socket.getInetAddress()+" : "+socket.getPort() ;
    }
}
