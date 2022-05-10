package com.junt.audio;

import android.media.AudioRecord;
import android.util.Log;

/**
 * 音频录制管理类
 */
public class AudioRecorder {
    private final String TAG = "Audio-recorder";
    private static AudioRecorder audioRecorderInstance;

    /**
     * 音频录制
     */
    private AudioRecord audioRecord;
    /**
     * 音效控制
     */
    private AudioEffectController audioEffectController;
    private int minBufferSize;
    /**
     * 是否正在录制
     */
    private boolean isRecording;
    /**
     * 音频编码器
     */
    private AudioEncoder audioEncoder;


    public static AudioRecorder getInstance() {
        if (audioRecorderInstance == null) {
            audioRecorderInstance = new AudioRecorder();
        }
        return audioRecorderInstance;
    }

    private AudioRecorder() {
        initAudioRecorder();
    }

    /**
     * 录音线程Runnable
     */
    private final Runnable recordRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                audioRecord.startRecording();
                Log.i(TAG, "run: 开始录音");
                byte[] pcmData = new byte[minBufferSize];

                while (isRecording && audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                    int readSize = audioRecord.read(pcmData, 0, pcmData.length);
                    if (readSize > 0) {
                        byte[] pcmCacheData = new byte[readSize];
                        System.arraycopy(pcmData, 0, pcmCacheData, 0, readSize);
                        //入队编码
                        audioEncoder.enqueue(pcmCacheData);
                    }
                }
                Log.i(TAG, "run: stop recording");

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                clear();
            }
        }
    };

    /**
     * 资源释放
     */
    private void clear() {
        audioRecord.stop();
        audioRecord.release();
        audioEffectController.release();
        audioEncoder.stopEncode();
    }

    /**
     * 开始录音
     */
    public void startRecord(OnReceiveDataListener listener) {
        Log.i(TAG, "startRecord: ");
        isRecording = true;
        audioEffectController.effect(true);
        audioEncoder = new AudioEncoder(listener);
        audioEncoder.startEncode();
        ThreadPoolService.getInstance().execute(recordRunnable);
    }

    /**
     * 停止录音
     */
    public void stopRecord() {
        isRecording = false;
        audioEffectController.effect(false);
    }

    /**
     * 初始化AudioRecord
     */
    private void initAudioRecorder() {
        Log.i(TAG, "initAudioRecorder: ");
        minBufferSize = AudioRecord.getMinBufferSize(EncoderConfig.SAMPLE_RATE,
                EncoderConfig.CHANNEL_CONFIG_IN, EncoderConfig.AUDIO_FORMAT);

        audioRecord = new AudioRecord(
                EncoderConfig.AUDIO_SOURCE,
                EncoderConfig.SAMPLE_RATE,
                EncoderConfig.CHANNEL_CONFIG_IN,
                EncoderConfig.AUDIO_FORMAT,
                minBufferSize);
        audioEffectController = new AudioEffectController(audioRecord.getAudioSessionId());
    }
}
