package com.example.hellov8;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class MyJobService extends JobService {

    private static final String TAG = "FCM";

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG, "Job started");

        Toast.makeText(this, "Job started", Toast.LENGTH_SHORT).show();

        // 在这里执行需要的后台任务，比如重启服务
        restartMyForegroundService();

        // 任务完成后调用 jobFinished
        jobFinished(params, false);
        return true; // 表示任务仍在进行
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "Job stopped");
        return true; // 重新调度
    }

    private void restartMyForegroundService() {
        Intent serviceIntent = new Intent(this, MyForegroundService.class);
        startService(serviceIntent);
    }
}
