package com.example.api;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.api.DownloadService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class DownLoadActivity extends AppCompatActivity implements View.OnClickListener {
    private ProgressBar progressBar;
    TextView updateinfo;
    String aimurl;
    String content;
    String version;
    private updateUiListener listener = new updateUiListener() {
        @Override
        public void onUiProgress(int progress) {
            progressBar.setProgress(progress);
            Log.d("debug", String.valueOf(progress));
        }

    };
    private DownloadService.DownloadBinder downloadBinder;//活动可调用服务中DownloadBinder类中的方法
    private ServiceConnection connection = new ServiceConnection() {
        //活动与服务绑定时调用，将服务中的Binder传给活动，从而活动通过binder操作服务
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            downloadBinder = (DownloadService.DownloadBinder) service;
            downloadBinder.setuilistener(listener);
        }

        //活动与服务断开时调用
        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_down_load);
        Button startDownLoad = (Button) findViewById(R.id.start_download);
        Button pauseDownLoad = (Button) findViewById(R.id.pause_download);
        Button cancelDownLoad = (Button) findViewById(R.id.cancel_download);
        updateinfo = (TextView) findViewById(R.id.updateinfo);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        startDownLoad.setOnClickListener(this);
        pauseDownLoad.setOnClickListener(this);
        cancelDownLoad.setOnClickListener(this);

        Intent beforeintent = getIntent();//获取main活动传来的数据
        version = beforeintent.getStringExtra("version");
        aimurl = beforeintent.getStringExtra("address");
        content = beforeintent.getStringExtra("content");
        updateinfo.setText("当前版本:1.0\n最新版本:" + version + "\n[更新内容]\n" + content);

        Intent intent = new Intent(this, DownloadService.class);
        startService(intent);//启动下载服务
        bindService(intent, connection, BIND_AUTO_CREATE);//活动与服务通信


        //动态申请SD卡读取权限
        if (ContextCompat.checkSelfPermission(com.example.api.DownLoadActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(com.example.api.DownLoadActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
        //不需要申请读取权限？
    }

    @Override
    public void onClick(View v) {
        Log.d("begin", "1");
        if (downloadBinder == null) {
            Log.d("begin", "2");
            return;
        }
        switch (v.getId()) {
            case R.id.start_download:
                Log.d("begin", "3");
                String url = aimurl;
                downloadBinder.startDownload(url);//开始下载
                break;
            case R.id.pause_download:
                downloadBinder.pauseDownload();
                break;
            case R.id.cancel_download:
                downloadBinder.cancelDownload();
                break;
            default:
                break;
        }
    }

    //处理申请权限的结果
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "拒绝权限将无法使用程序", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
    }

    @Override
    public void onStop() {
        super.onStop();

        Log.d("debug", "onStop");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("version", version);
        outState.putString("address", aimurl);
        outState.putString("content", content);
        Log.d("debug", "onSaveInstanceState");
    }

}