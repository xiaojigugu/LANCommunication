package com.junt.socket.socket;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.LinkedBlockingQueue;

public class LiveClient {
    private final String TAG = "client";

    private OnSocketListener listener;
    private String ip;
    private int portServer, portClient;
    private DatagramSocket socket;
    private LinkedBlockingQueue<byte[]> outQueue;

    public LiveClient(OnSocketListener listener) {
        this.listener = listener;
        outQueue = new LinkedBlockingQueue<>(1000);
    }

    public void connectServer(final String ip, int portServer, final int portClient) {
        this.ip = ip;
        this.portServer = portServer;
        this.portClient = portClient;
        ThreadPool.getInstance().execute(connectRunnable);
    }

    public void stop() {
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }

    /**
     * 连接用Runnable
     */
    private Runnable connectRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                Log.i(TAG, "run: 启动本地客户端");
                socket = new DatagramSocket(portClient);
                if (socket.isBound()) {
                    Log.i(TAG, "run: 启动成功");
                    listener.onConnected();
                    ThreadPool.getInstance().execute(sendRunnable);
                } else {
                    Log.i(TAG, "run: 连接失败");
                    listener.onClosed("连接失败");
                }
            } catch (IOException e) {
                e.printStackTrace();
                listener.onClosed(e.getMessage());
            }
        }
    };

    private final Runnable sendRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                DatagramPacket outPacket;
                InetAddress serverAddress = InetAddress.getByName(ip);
                byte[] outData;
                while (socket.isBound() && !socket.isClosed()) {
                    try {
                        outData = outQueue.take();
                        Log.i(TAG, "run: 2.压缩数据发送：" + outData.length);
                        outPacket = new DatagramPacket(outData, outData.length, serverAddress, portServer);
                        socket.send(outPacket);
                    } catch (Exception e) {
                        Log.e(TAG, "run: sendRunnable-->" + e.getMessage());
                    }
                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }
    };


    public void sendData(byte[] data) {
        try {
            if (socket != null && socket.isBound() && !socket.isClosed()) {
                Log.i(TAG, "1.压缩数据入队:" + data.length);
                outQueue.put(data);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        return socket.isBound() && !socket.isClosed();
    }
}
