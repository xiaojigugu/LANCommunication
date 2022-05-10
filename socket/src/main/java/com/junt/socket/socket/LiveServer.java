package com.junt.socket.socket;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 服务端封装
 */
public class LiveServer {
    private final String TAG = "server";

    private OnServerListener listener;
    private int port;
    private boolean isReceiving;
    private DatagramSocket serverSocket;
    private LinkedBlockingQueue<byte[]> inQueue;

    public LiveServer(OnServerListener listener) {
        this.listener = listener;
        inQueue = new LinkedBlockingQueue<>(1000);
    }

    public void startServer(int port) {
        this.port = port;
        ThreadPool.getInstance().execute(connectRunnable);
    }

    private Runnable connectRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                Log.i(TAG, "run: 启动本地服务");
                serverSocket = new DatagramSocket(port);
                serverSocket.setReuseAddress(true);
                if (serverSocket.isBound()) {
                    Log.i(TAG, "run: 服务已开启");
                    listener.onBound();
                    isReceiving = true;
                    ThreadPool.getInstance().execute(receiveRunnable);
                    ThreadPool.getInstance().execute(handleDataRunnable);
                } else {
                    Log.i(TAG, "run: 服务开启失败");
                    listener.onClosed("本地服务启动失败");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    private Runnable receiveRunnable = new Runnable() {
        @Override
        public void run() {
            byte[] data;
            byte[] receiveData;
            while (isReceiving && serverSocket.isBound() && !serverSocket.isClosed()) {
                try {
                    Log.i(TAG, "run: 等待接收数据");
                    data = new byte[1024];
                    DatagramPacket datagramPacket = new DatagramPacket(data, data.length);
                    serverSocket.receive(datagramPacket);
                    receiveData = new byte[datagramPacket.getLength()];
                    System.arraycopy(datagramPacket.getData(), 0, receiveData, 0, datagramPacket.getLength());
                    inQueue.put(receiveData);
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private Runnable handleDataRunnable = new Runnable() {
        @Override
        public void run() {
            while (serverSocket.isBound() && !serverSocket.isClosed()) {
                try {
                    byte[] inData = inQueue.take();
                    listener.onReceive(inData);
                } catch (InterruptedException e) {
                    Log.e(TAG, "run: handleDataRunnable-->" + e.getMessage());
                }
            }
        }
    };

    public boolean isOpen() {
        return serverSocket.isBound() && !serverSocket.isClosed();
    }

    public void stop() {
        isReceiving = false;
        if (serverSocket != null) {
            serverSocket.close();
        }
    }
}
