package com.example.hellov8;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import androidx.core.content.ContextCompat;

public class StaticNetworkChangeReceiver extends BroadcastReceiver {

    private static final String TAG = "FCM";

    @Override
    public void onReceive(Context context, Intent intent) {
        // 检查网络是否可用

        Log.d(TAG, "Static Network Changed: " + intent.getDataString());

        if (isNetworkAvailable(context)) {
            // 启动前台服务
            Intent serviceIntent = new Intent(context, MyForegroundService.class);
            ContextCompat.startForegroundService(context, serviceIntent);
        }
    }

    // 检查网络连接状态
    private boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnected();
        }
        return false;
    }
}
