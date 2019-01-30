package ru.chat.server;
import ru.chat.network.TCPConnection;
import ru.chat.network.TCPConnectionListener;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

public class ChatServer implements TCPConnectionListener {
    public static void main(String[] args) {
        new ChatServer();

    }
    private final ArrayList<TCPConnection> tcpConnections = new ArrayList<>();

    private ChatServer(){
        System.out.println("Server running");
        try (ServerSocket serverSocket= new ServerSocket(1234)){
            while (true){
                try {
                    new TCPConnection(serverSocket.accept(), this);
                }catch (IOException e){
                    System.out.println("ru.chat.network.TCPConnection exception "+e);
                }
            }
        }
        catch (IOException e) {
            throw new RuntimeException();
        }
    }
    @Override
    public synchronized void onConnectionReady(TCPConnection tcpConnection) {
        tcpConnections.add(tcpConnection);
        sendToAllConnections("Client connected: "+tcpConnection);
    }

    @Override
    public synchronized void onReceiveString(TCPConnection tcpConnection, String value) {
        sendToAllConnections(value);
    }

    @Override
    public synchronized void onDisconnection(TCPConnection tcpConnection) {
        tcpConnections.remove(tcpConnection);
        sendToAllConnections("Client disconnected: "+tcpConnection);
    }

    @Override
    public synchronized void onException(TCPConnection tcpConnection, Exception e) {
        System.out.println("TCPException: "+e);
    }
    private void sendToAllConnections(String value){
        System.out.println(value);
        for (int i=0; i<tcpConnections.size(); i++) tcpConnections.get(i).sendString(value);
    }
}
