package com.junt.audio;

import android.media.audiofx.AcousticEchoCanceler;
import android.media.audiofx.AutomaticGainControl;
import android.media.audiofx.NoiseSuppressor;
import android.util.Log;

/**
 * Audio音效控制
 * 噪音抑制,回声消除,自动增强
 */
public class AudioEffectController {
    private final String TAG = "音效";
    /**
     * 噪音抑制
     */
    private NoiseSuppressor noiseSuppressor;
    /**
     * 回声消除
     */
    private AcousticEchoCanceler acousticEchoCanceler;
    /**
     * 自动增强
     */
    private AutomaticGainControl automaticGainControl;

    /**
     * @param audioRecordSessionId The audio session is retrieved
     *                             by calling {@link android.media.AudioRecord#getAudioSessionId()}
     *                             on the AudioRecord instance.
     */
    public AudioEffectController(int audioRecordSessionId) {
        if (NoiseSuppressor.isAvailable()) {
            noiseSuppressor = NoiseSuppressor.create(audioRecordSessionId);
            Log.i(TAG, "AudioEffectController: 噪音抑制初始化");
        }

        if (AcousticEchoCanceler.isAvailable()) {
            acousticEchoCanceler = AcousticEchoCanceler.create(audioRecordSessionId);
            Log.i(TAG, "AudioEffectController: 回声消除初始化");
        }

        if (AutomaticGainControl.isAvailable()) {
            automaticGainControl = AutomaticGainControl.create(audioRecordSessionId);
            Log.i(TAG, "AudioEffectController: 自动增强初始化");
        }
    }

    /**
     * 开启/关闭 音效
     *
     * @param enable true - 开启   false - 关闭
     */
    public void effect(boolean enable) {
        if (noiseSuppressor != null) {
            noiseSuppressor.setEnabled(enable);
            Log.i(TAG, "effect: 噪音抑制=" + enable);
        }
        if (acousticEchoCanceler != null) {
            acousticEchoCanceler.setEnabled(enable);
            Log.i(TAG, "effect: 回声消除=" + enable);
        }
        if (automaticGainControl != null) {
            automaticGainControl.setEnabled(enable);
            Log.i(TAG, "effect: 自动增强=" + enable);
        }
    }

    /**
     * 释放资源
     */
    public void release() {
        if (noiseSuppressor != null) {
            noiseSuppressor.release();
        }
        if (acousticEchoCanceler != null) {
            acousticEchoCanceler.release();
        }
        if (automaticGainControl != null) {
            automaticGainControl.release();
        }
        Log.i(TAG, "release: ");
    }
}
