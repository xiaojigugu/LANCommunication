package com.junt.audio;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Build;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * 音频解码器
 */
public class AudioDecoder {
    private final String TAG = "Audio-decoder";

    private MediaCodec mediaCodec;
    private boolean isDecoding;
    private final AudioQueue compressedQueue;
    private final OnReceiveDataListener listener;


    public AudioDecoder(OnReceiveDataListener listener) {
        this.compressedQueue = new AudioQueue();
        this.listener = listener;
    }

    public void startDecoder() {
        isDecoding = true;
        initAudioDecoder();
        ThreadPoolService.getInstance().execute(encodeRunnable);
    }

    private final Runnable encodeRunnable = new Runnable() {
        @Override
        public void run() {
            ByteBuffer[] inputBuffers = mediaCodec.getInputBuffers();
            ByteBuffer[] outputBuffers = mediaCodec.getOutputBuffers();
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            byte[] compressedData;
            ByteBuffer inputBuffer;

            while (isDecoding) {
                try {
                    compressedData = compressedQueue.dequeue();
                    Log.i(TAG, "run: 拿到压缩数据" + compressedData.length);
                    int decodeInputIndex = mediaCodec.dequeueInputBuffer(-1);
                    if (decodeInputIndex >= 0) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            inputBuffer = mediaCodec.getInputBuffer(decodeInputIndex);
                        } else {
                            inputBuffer = inputBuffers[decodeInputIndex];
                        }
                        inputBuffer.clear();
                        inputBuffer.position(0);
                        inputBuffer.limit(compressedData.length);
                        inputBuffer.put(compressedData, 0, compressedData.length);

                        mediaCodec.queueInputBuffer(decodeInputIndex, 0, compressedData.length,
                                System.currentTimeMillis(), 0);
                    }
                    int outputIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 10000);
                    if (outputIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                        outputBuffers = mediaCodec.getOutputBuffers();
                        outputIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 10000);
                    }
                    while (outputIndex >= 0) {
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                            outputBuffer = mediaCodec.getOutputBuffer(decodeInputIndex);
//                        } else {
                        ByteBuffer outputBuffer = outputBuffers[outputIndex];
//                        }
                        outputBuffer.position(bufferInfo.offset);
                        outputBuffer.limit(bufferInfo.offset + bufferInfo.size);
                        byte[] encodeData = new byte[bufferInfo.size];
                        outputBuffer.get(encodeData, 0, bufferInfo.size);
                        //拿到了解码数据,回调出去进行播放
                        listener.onReceive(encodeData, bufferInfo.offset);
                        //释放当前内存
                        mediaCodec.releaseOutputBuffer(outputIndex, false);
                        outputIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 10000);
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                    Log.e(TAG, "run: 解码错误" + e.getMessage());
                }
            }
            Log.i(TAG, "run: stop decoding");
            clear();
        }
    };

    /**
     * 资源释放
     */
    private void clear() {
        try {
            compressedQueue.clear();
            mediaCodec.stop();
            mediaCodec.release();
        } catch (Exception e) {
            Log.e(TAG, "clear：" + e.getMessage());
        }
    }

    /**
     * 停止编码
     */
    public void stopDecoder() {
        isDecoding = false;
    }

    private void initAudioDecoder() {
        try {
            MediaFormat audioFormat = MediaFormat.createAudioFormat(EncoderConfig.MIME_TYPE,
                    EncoderConfig.SAMPLE_RATE, EncoderConfig.CHANNEL_COUNT);
            audioFormat.setInteger(MediaFormat.KEY_BIT_RATE, EncoderConfig.KEY_BIT_RATE);
            audioFormat.setInteger(MediaFormat.KEY_IS_ADTS, 1);
            audioFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
            ByteBuffer csd_0 = ByteBuffer.wrap(EncoderConfig.csd_0);
            audioFormat.setByteBuffer("csd-0", csd_0);

            mediaCodec = MediaCodec.createDecoderByType(EncoderConfig.MIME_TYPE);
            mediaCodec.configure(audioFormat, null, null, 0);
            mediaCodec.start();
        } catch (IOException e) {
            Log.e(TAG, "initAudioEncoder: error" + e.getMessage());
        }
    }

    public void enqueue(byte[] data) {
        if (isDecoding) {
            compressedQueue.enqueue(data);
        }
    }

    private byte[] getCsd_0() {
        int profile = 2; //AAC LC
        int freqIdx = 4; //44100 根据不同的采样率修改这个值
        int chanCfg = 1; //CPE
        byte[] csd_0 = new byte[2];
        csd_0[0] = (byte) ((((profile + 1) << 3) & 0xff) | ((freqIdx >> 1) & 0xff));
        csd_0[1] = (byte) (((freqIdx << 7) & 0xff) | ((chanCfg << 3) & 0xff));
        return csd_0;
    }
}


