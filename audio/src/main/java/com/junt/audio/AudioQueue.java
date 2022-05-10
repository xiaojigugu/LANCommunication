package com.junt.audio;

import java.util.concurrent.LinkedBlockingDeque;

/**
 * 音频队列
 */
public class AudioQueue {
    private LinkedBlockingDeque<byte[]> queue;

    public AudioQueue() {
        queue = new LinkedBlockingDeque<>(1000);
    }

    /**
     * 数据入队，队列已满时将阻塞
     *
     * @param data 字节数据
     */
    public void enqueue(byte[] data) {
        try {
            queue.put(data);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 音频数据出队，队列为空时阻塞
     * @return pcm原始数据
     */
    public byte[] dequeue(){
        try {
            return queue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 清除队列
     */
    public void clear(){
        queue.clear();
    }

    /**
     * 队列是否为空
     */
    public boolean isEmpty(){
        return queue.isEmpty();
    }

}
