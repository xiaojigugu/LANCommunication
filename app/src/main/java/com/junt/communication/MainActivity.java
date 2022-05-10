package com.junt.communication;

import android.Manifest;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.junt.audio.AudioPlay;
import com.junt.audio.AudioRecorder;
import com.junt.audio.OnReceiveDataListener;
import com.junt.socket.socket.LiveSocket;
import com.junt.socket.socket.OnServerListener;
import com.junt.socket.socket.OnSocketListener;

/**
 * 客户端
 */
public class MainActivity extends AppCompatActivity {
    private final String TAG = "Audio-main";
    private final int PORT_SERVER = 10001;
    private final int PORT_CLIENT = 10002;
    private EditText etIp;
    private TextView tvClientStatus, tvServerStatus;

    private LiveSocket liveSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // play audio with max volume
        AudioManager audioManager= (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),1);

        requestPermission();

        etIp = findViewById(R.id.etIp);

        tvClientStatus = findViewById(R.id.tvClientStatus);
        tvServerStatus = findViewById(R.id.tvServerStatus);

        startLocalServer();
    }

    private void requestPermission() {
        String[] permissions = {Manifest.permission.RECORD_AUDIO, Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.MODIFY_AUDIO_SETTINGS};
        ActivityCompat.requestPermissions(this, permissions, 99);
    }

    private void startLocalServer() {
        OnSocketListener clientListener = new OnSocketListener() {
            @Override
            public void onConnected() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvClientStatus.setText("已连接");
                    }
                });
            }

            @Override
            public void onClosed(final String message) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvClientStatus.setText(message);
                    }
                });
            }

            @Override
            public void onReceive(byte[] data) {
                Log.i(TAG, "client.onReceive: " + data.length);
                AudioPlay.getInstance().enqueue(data);
            }
        };
        OnServerListener serverListener = new OnServerListener() {
            @Override
            public void onBound() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvServerStatus.setText("本地服务已开启");
                    }
                });
            }

            @Override
            public void onClosed(String message) {
                tvServerStatus.setText("本地服务已关闭");
            }

            @Override
            public void onReceive(byte[] data) {
                AudioPlay.getInstance().enqueue(data);
            }
        };
        liveSocket = new LiveSocket(clientListener, serverListener);
        liveSocket.startServer(PORT_SERVER);
    }

    /**
     * 关闭音频通话
     */
    public void stopAudioRecord(View view) {
        AudioRecorder.getInstance().stopRecord();
        AudioPlay.getInstance().stop();
    }

    /**
     * 开启音频通话
     */
    public void startAudioRecord(View view) {
        if (!liveSocket.isServerConnected()) {
            Log.i(TAG, "startAudioRecord: 尚未连接到服务器");
            return;
        }
        //开启播放录音
        AudioPlay.getInstance().start();

        //开启录音、编码、发送
        AudioRecorder.getInstance().startRecord(new OnReceiveDataListener() {
            @Override
            public void onReceive(byte[] data, int offset) {
                //发送编码数据
                liveSocket.sendData(data);
            }
        });
    }

    /**
     * 连接服务端
     */
    public void connectServer(View view) {
        String ip = etIp.getText().toString().trim();

        if (TextUtils.isEmpty(ip)) {
            Toast.makeText(this, "ip错误", Toast.LENGTH_SHORT).show();
            return;
        }
        liveSocket.connectServer(ip, PORT_SERVER, PORT_CLIENT);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME) {
            finish();
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        AudioRecorder.getInstance().stopRecord();
        AudioPlay.getInstance().destroy();
        liveSocket.stop();
        super.onDestroy();
        Log.i(TAG, "onDestroy: ");
    }


}