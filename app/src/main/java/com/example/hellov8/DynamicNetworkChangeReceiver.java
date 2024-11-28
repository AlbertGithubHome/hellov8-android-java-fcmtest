package com.example.hellov8;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

public class DynamicNetworkChangeReceiver extends BroadcastReceiver {

    private static final String TAG = "FCM";

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d(TAG, "Dynamic Network Changed info: " + intent.getScheme());

        if (isNetworkAvailable(context)) {
            Toast.makeText(context, "网络已连接", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "网络已断开", Toast.LENGTH_SHORT).show();
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
