package com.junt.audio;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

/**
 * 音频播放
 */
public class AudioPlay {
    private final String TAG = "Audio-play";
    private static AudioPlay audioPlayInstance;

    private AudioTrack audioTrack;
    private AudioDecoder audioDecoder;

    public AudioPlay() {
    }

    public static AudioPlay getInstance() {
        if (audioPlayInstance == null) {
            audioPlayInstance = new AudioPlay();
        }
        return audioPlayInstance;
    }

    private void init() {
        try {
            int minSize = AudioTrack.getMinBufferSize(EncoderConfig.SAMPLE_RATE, EncoderConfig.CHANNEL_CONFIG_OUT, EncoderConfig.AUDIO_FORMAT);
            audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, EncoderConfig.SAMPLE_RATE,
                    EncoderConfig.CHANNEL_CONFIG_OUT, EncoderConfig.AUDIO_FORMAT, minSize * 4, AudioTrack.MODE_STREAM);
            audioTrack.setStereoVolume(AudioTrack.getMaxVolume(), AudioTrack.getMaxVolume());
            audioDecoder = new AudioDecoder(new OnReceiveDataListener() {
                @Override
                public void onReceive(byte[] data, int offset) {
                    //收到解码后的pcm数据
                    Log.i(TAG, "收到解码数据，入队播放:" + offset + "/" + data.length);
                    play(data, offset);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "init: error=" + e.getMessage());
        }
    }

    public void enqueue(byte[] data) {
        if (audioDecoder != null) {
            Log.i(TAG, "enqueue: 收到压缩数据入队" + data.length);
            audioDecoder.enqueue(data);
        }
    }

    /**
     * 播放音频
     *
     * @param data pcm数据
     */
    public void play(byte[] data, int offset) {
        audioTrack.write(data, offset, data.length);
    }

    /**
     * 启动音频播放
     */
    public void start() {
        Log.i(TAG, "start: ");
        init();

        audioTrack.play();
        audioDecoder.startDecoder();
    }

    public void stop() {
        if (audioTrack != null && audioDecoder != null) {
            audioTrack.stop();
            audioDecoder.stopDecoder();
        }
    }

    public void destroy() {
        if (audioTrack != null && audioDecoder != null) {
            stop();
            audioTrack.release();
        }
    }
}
