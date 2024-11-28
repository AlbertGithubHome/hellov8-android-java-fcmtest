package com.example.hellov8;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.app.PendingIntent;

public class MyForegroundService extends Service {
    private static final String CHANNEL_ID = "MyForegroundServiceChannel";

    @Override
    public void onCreate() {
        super.onCreate();

//        // 创建通知渠道 (Android 8.0 及以上需要)
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            NotificationChannel channel = new NotificationChannel(
//                    CHANNEL_ID,
//                    "Foreground Service Channel",
//                    NotificationManager.IMPORTANCE_DEFAULT
//            );
//            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//            notificationManager.createNotificationChannel(channel);
//        }
//
//        // 创建并显示通知
//        Notification notification = new Notification.Builder(this, CHANNEL_ID)
//                .setContentTitle("My Foreground Service")
//                .setContentText("Service is running in the foreground")
//                .setSmallIcon(R.drawable.ic_launcher_foreground)
//                .build();
//
//        // 启动前台服务
//        startForeground(1, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // 创建通知渠道
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(channel);

        // 创建意图
        Intent notificationIntent  = new Intent(this, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // 创建PendingIntent
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        // 创建并显示通知
        Notification notification = new Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("My Foreground Service")
                .setContentText("Service is running in the foreground")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent) // 设置点击通知时的意图
                .setAutoCancel(true) // 点击后自动消失
                .build();

        // 启动前台服务
        startForeground(1, notification);

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 停止服务时取消通知
        stopForeground(true);
    }
}


