package com.junt.audio;

import android.media.AudioFormat;
import android.media.MediaFormat;
import android.media.MediaRecorder;

public class EncoderConfig {
    public static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    /**
     * 音频来源麦克风
     */
    public static final int AUDIO_SOURCE = MediaRecorder.AudioSource.VOICE_COMMUNICATION;

    /**
     * 采样率
     */
    public static int SAMPLE_RATE = 16000;
    /**
     * adts采样频率Index:
     * 0: 96000 Hz
     * 1: 88200 Hz
     * 2: 64000 Hz
     * 3: 48000 Hz
     * 4: 44100 Hz
     * 5: 32000 Hz
     * 6: 24000 Hz
     * 7: 22050 Hz
     * 8: 16000 Hz
     * 9: 12000 Hz
     * 10: 11025 Hz
     * 11: 8000 Hz
     * 12: 7350 Hz
     * 13: Reserved
     * 14: Reserved
     * 15: frequency is written explictly
     */
    public static int SAMPLE_RATE_FREQUENCY_INDEX = 8;
    public static int KEY_BIT_RATE = 96000;

//    //  双声道
//    public static final int CHANNEL_CONFIG_IN = AudioFormat.CHANNEL_IN_STEREO;
//    public static final int CHANNEL_CONFIG_OUT = AudioFormat.CHANNEL_OUT_STEREO;
//    public static int CHANNEL_COUNT = 2;
//    //计算方式见 README
//    public static byte[] csd_0 = new byte[]{0x12, 0x10};

    //    单声道
    public static final int CHANNEL_CONFIG_IN = AudioFormat.CHANNEL_IN_MONO;
    public static final int CHANNEL_CONFIG_OUT = AudioFormat.CHANNEL_OUT_MONO;
    public static int CHANNEL_COUNT = 1;
    //计算方式见 README
    public static byte[] csd_0 = new byte[]{0x14, 0x08};

    /**
     * 音频编码格式
     */
    public static final String MIME_TYPE = MediaFormat.MIMETYPE_AUDIO_AAC;

}
