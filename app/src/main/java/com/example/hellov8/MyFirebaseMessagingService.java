package com.example.hellov8;

import android.util.Log; // 用于日志打印
import com.google.firebase.messaging.FirebaseMessagingService; // Firebase 消息服务基类
import com.google.firebase.messaging.RemoteMessage; // 表示接收到的消息

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FCM";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // 打印接收到的消息
        if (remoteMessage.getNotification() != null) {
            String messageBody = remoteMessage.getNotification().getBody();
            Log.d(TAG, "Message Notification Body: " + messageBody);
        }
    }

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d(TAG, "New FCM Token: " + token);

        // 发送Token到你的服务器
        sendTokenToServer(token);
    }

    private void sendTokenToServer(String token) {
        // 自定义逻辑：将新Token发送到你的后端服务器
    }
}