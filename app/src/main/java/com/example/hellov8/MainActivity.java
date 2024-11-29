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
import android.net.Uri;
import android.provider.Settings;
import android.content.pm.PackageManager;
import android.Manifest;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;



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

        // 在启动时弹出提示
        //showAutoStartPermissionDialog();

        // 引导开启电池优化
        openBatteryOptimizationSettings(this);
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

    private void showAutoStartPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("开启自启动权限")
                .setMessage("为确保应用正常运行，请手动开启自启动权限。")
                .setPositiveButton("去设置", (dialog, which) -> {
                    openAutoStartSetting(MainActivity.this); // 调用方法
                })
                .setNegativeButton("稍后再说", (dialog, which) -> dialog.dismiss())
                .show();
    }

    /**
     * 引导开通-电池管理优化
     * 申请加入白名单
     两种方法,可以看自己使用哪种
     * */
    public static void openBatteryOptimizationSettings(Context context) {
        try {
            // 方法1：打开电池优化设置界面
             Intent intent = new Intent();
             intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
             context.startActivity(intent);

            // 方法2：触发系统对话框
//            Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
//            intent.setData(Uri.parse("package:" + context.getPackageName()));
//            context.startActivity(intent);

            Log.d("FCM", "openBatteryOptimizationSettings_e=" + context.getPackageName());
        } catch (Exception e) {
            Log.e("FCM", "openBatteryOptimizationSettings_e=" + e.getLocalizedMessage());
        }
    }

    // 跳转到自启动设置的方法
    public static void openAutoStartSetting(Context context) {
        try {
            Intent intent = new Intent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            String manufacturer = android.os.Build.MANUFACTURER.toLowerCase();

            Log.d(TAG, "Ready to open auto start setting with manufacturer: " + manufacturer);

            switch (manufacturer) {
                case "xiaomi": // 小米
                    intent.setComponent(new ComponentName("com.miui.securitycenter",
                            "com.miui.permcenter.autostart.AutoStartManagementActivity"));
                    break;
                case "huawei": // 华为
                case "honor": // 荣耀
                    intent.setComponent(new ComponentName("com.huawei.systemmanager",
                            "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"));
                    break;
                case "oppo": // OPPO
//                    intent.setComponent(new ComponentName("com.coloros.safecenter",
//                            "com.coloros.safecenter.permission.startup.StartupAppListActivity"));
//                    intent.setComponent(new ComponentName("com.coloros.oppoguardelf",
//                            "com.coloros.powermanager.fuelgaue.PowerUsageModelActivity"));
                    intent.setComponent(new ComponentName("com.oplus.battery",
                            "com.oplus.startupapp.view.StartupAppListActivity"));
                    break;
                case "vivo": // Vivo
                    intent.setComponent(new ComponentName("com.iqoo.secure",
                            "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity"));
                    break;
                case "samsung": // 三星
                    intent.setComponent(new ComponentName("com.samsung.android.sm",
                            "com.samsung.android.sm.app.dashboard.SmartManagerDashBoardActivity"));
                    break;
                default:
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.fromParts("package", context.getPackageName(), null));
                    break;
            }
            Log.d(TAG, "Before start auto start setting.");
            context.startActivity(intent);
            Log.d(TAG, "After start auto start setting.");
        } catch (Exception e) {
            e.printStackTrace();

            Log.d(TAG, "Catch exception:" + e.toString());

            // 如果跳转失败，打开常规设置界面
            Intent fallbackIntent = new Intent(Settings.ACTION_SETTINGS);
            fallbackIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(fallbackIntent);

            Log.d(TAG, "Not found the auto start setting for this phone.");
        }
    }


}