package com.junt.socket.socket;

/**
 * 客户端\服务端封装
 */
public class LiveSocket {

    private final LiveClient liveClient;
    private final LiveServer liveServer;


    public LiveSocket(OnSocketListener clientListener, OnServerListener serverListener) {
        liveClient = new LiveClient(clientListener);
        liveServer = new LiveServer(serverListener);
    }

    public void startServer(int port) {
        liveServer.startServer(port);
    }

    public void stopServer() {
        liveServer.stop();
    }

    public void connectServer(String ip, int PORT_SERVER, int portClient) {
        liveClient.connectServer(ip, PORT_SERVER, portClient);
    }

    public void sendData(byte[] data) {
        liveClient.sendData(data);
    }


    public boolean isServerConnected() {
        return liveClient.isConnected();
    }

    public void stop() {
        liveClient.stop();
        liveServer.stop();
    }
}
