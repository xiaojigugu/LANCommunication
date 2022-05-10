package com.junt.audio;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Build;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * 音频编码
 */
public class AudioEncoder {
    private final String TAG = "Audio-encoder";

    private MediaCodec mediaCodec;
    private boolean isEncoding;
    private final AudioQueue pcmQueue;
    private final OnReceiveDataListener listener;


    public AudioEncoder(OnReceiveDataListener listener) {
        pcmQueue = new AudioQueue();
        this.listener = listener;
    }

    public void enqueue(byte[] data) {
        if (isEncoding) {
            pcmQueue.enqueue(data);
        }
    }


    private final Runnable encodeRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                Log.i(TAG, "run: 开始编码");
                ByteBuffer[] inputBuffers = mediaCodec.getInputBuffers();
                ByteBuffer[] outputBuffers = mediaCodec.getOutputBuffers();

                MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                byte[] pcmData;
                ByteBuffer inputBuffer;
                ByteBuffer outputBuffer;
                while (isEncoding) {
                    pcmData = pcmQueue.dequeue();
                    int encodeInputIndex = mediaCodec.dequeueInputBuffer(-1);
                    if (encodeInputIndex >= 0) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            inputBuffer = mediaCodec.getInputBuffer(encodeInputIndex);
                        } else {
                            inputBuffer = inputBuffers[encodeInputIndex];
                        }
                        inputBuffer.clear();
                        inputBuffer.position(0);
                        inputBuffer.put(pcmData);
                        inputBuffer.limit(pcmData.length);
                        mediaCodec.queueInputBuffer(encodeInputIndex, 0, pcmData.length,
                                System.currentTimeMillis(), 0);
                    }

                    int outputIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 10000);
                    if (outputIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                        outputBuffers = mediaCodec.getOutputBuffers();
                        outputIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 10000);
                    }
                    while (outputIndex >= 0) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            outputBuffer = mediaCodec.getOutputBuffer(outputIndex);
                        } else {
                            outputBuffer = outputBuffers[outputIndex];
                        }

                        byte[] encodeData = new byte[7 + bufferInfo.size];
                        addADTStoPacket(encodeData, encodeData.length);

                        outputBuffer.position(bufferInfo.offset);
                        outputBuffer.limit(bufferInfo.offset + bufferInfo.size);
                        outputBuffer.get(encodeData, 7, bufferInfo.size);

                        mediaCodec.releaseOutputBuffer(outputIndex, false);
                        outputIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 10000);
                        //拿到了编码数据,推送出去
                        listener.onReceive(encodeData, bufferInfo.offset);
                    }
                }
                Log.i(TAG, "run: stop encoding");
            } catch (Throwable e) {
                Log.e(TAG, "run: 编码错误" + e);
            } finally {
                clear();
            }
        }
    };

    /**
     * 资源释放
     */
    private void clear() {
        pcmQueue.clear();
        mediaCodec.stop();
        mediaCodec.release();
    }

    public void startEncode() {
        Log.i(TAG, "startEncode: ");
        isEncoding = true;
        initAudioEncoder();
        ThreadPoolService.getInstance().execute(encodeRunnable);
    }

    /**
     * 停止编码
     */
    public void stopEncode() {
        isEncoding = false;
    }

    private void initAudioEncoder() {
        Log.i(TAG, "initAudioEncoder: ");
        try {
            MediaFormat audioFormat = MediaFormat.createAudioFormat(EncoderConfig.MIME_TYPE,
                    EncoderConfig.SAMPLE_RATE, EncoderConfig.CHANNEL_COUNT);
            audioFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
            audioFormat.setInteger(MediaFormat.KEY_BIT_RATE, EncoderConfig.KEY_BIT_RATE);
            audioFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 1024 * 1024);// It will

            mediaCodec = MediaCodec.createEncoderByType(EncoderConfig.MIME_TYPE);
            mediaCodec.configure(audioFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mediaCodec.start();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "initAudioEncoder: error" + e.getMessage());
        }
    }

    /**
     * 添加ADTS头
     * @param packet    数据包
     * @param packetLen 数据包长度
     */
    private void addADTStoPacket(byte[] packet, int packetLen) {
        int profile = 2; //AAC LC
        int freqIdx = EncoderConfig.SAMPLE_RATE_FREQUENCY_INDEX; //16000 根据不同的采样率修改这个值
        int chanCfg = EncoderConfig.CHANNEL_COUNT; //CPE
        packet[0] = (byte) 0xFF;
        packet[1] = (byte) 0xF9;
        packet[2] = (byte) (((profile - 1) << 6) + (freqIdx << 2) + (chanCfg >> 2));
        packet[3] = (byte) (((chanCfg & 3) << 6) + (packetLen >> 11));
        packet[4] = (byte) ((packetLen & 0x7FF) >> 3);
        packet[5] = (byte) (((packetLen & 7) << 5) + 0x1F);
        packet[6] = (byte) 0xFC;
    }
}
