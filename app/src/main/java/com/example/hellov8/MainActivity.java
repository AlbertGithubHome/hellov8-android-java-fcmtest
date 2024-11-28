package com.example.hellov8;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.content.ContextCompat;
import androidx.annotation.RequiresApi;

import android.content.pm.PackageManager;
import android.Manifest;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    private DynamicNetworkChangeReceiver networkChangeReceiver;
    private static final String TAG = "FCM";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 检查并申请通知权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1001);
            }
        }

        // 启动前台服务
        Intent serviceIntent = new Intent(this, MyForegroundService.class);
        startService(serviceIntent);

        // 注册动态网络变化广播接收器
        networkChangeReceiver = new DynamicNetworkChangeReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeReceiver, intentFilter);

        // 注册定时任务
        scheduleJob(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 解除广播注册
        if (networkChangeReceiver != null) {
            unregisterReceiver(networkChangeReceiver);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Notification通知权限已授权");
            } else {
                Log.d(TAG, "Notification通知权限被拒绝");
            }
        }
    }

    // 周期性任务的最小间隔: 在 Android 8.0（API 级别 26）及更高版本中，
    // 周期性任务的最小间隔为 15 分钟。设置的 1 分钟间隔将会被忽略，任务将按照系统的最小时间间隔（15分钟）来运行
    private void scheduleJob(Context context) {
        ComponentName componentName = new ComponentName(context, MyJobService.class);
        JobInfo jobInfo = new JobInfo.Builder(1, componentName)
//                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY) // 网络连接
//                .setRequiresCharging(true) // 设备充电
                .setPersisted(true) // 设备重启后仍然保持
                .setPeriodic(15 * 60 * 1000) // 每15分钟执行一次
                .build();

        Log.d(TAG, "创建schedule完成");

        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(jobInfo);
    }
}