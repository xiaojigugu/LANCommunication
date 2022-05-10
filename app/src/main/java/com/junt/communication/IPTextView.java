package com.junt.communication;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.AttributeSet;

/**
 * 自动更新ip
 */
public class IPTextView extends androidx.appcompat.widget.AppCompatTextView {

    private WifiManager wifiManager;

    public IPTextView(Context context) {
        super(context);
    }

    public IPTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    public IPTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (wifiManager!=null){
            WifiInfo connectionInfo = wifiManager.getConnectionInfo();
            setText(String.format("%s--%s--%s",
                    connectionInfo.getSSID(),
                    connectionInfo.getBSSID(),
                    toIpStr(connectionInfo.getIpAddress())));
        }
    }

    private String toIpStr(int ip) {
        return (ip & 0xFF) + "." + ((ip >> 8) & 0xFF) + "." + ((ip >> 16) & 0xFF) + "."
                + ((ip >> 24) & 0xFF);
    }
}
