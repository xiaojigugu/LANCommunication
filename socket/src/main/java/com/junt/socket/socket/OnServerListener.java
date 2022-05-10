package com.junt.socket.socket;

public interface OnServerListener {
    void onBound();
    void onClosed(String message);
    void onReceive(byte[] data);
}
