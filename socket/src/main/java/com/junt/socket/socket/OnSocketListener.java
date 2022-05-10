package com.junt.socket.socket;

public interface OnSocketListener {
    void onConnected();
    void onClosed(String message);
    void onReceive(byte[] data);
}
