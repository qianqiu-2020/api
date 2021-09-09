package com.example.api;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.example.api.DownloadListener;
import com.example.api.DownloadTask;

import java.io.File;

public class DownloadService extends Service {
    private DownloadTask downloadTask;
    private String downloadUrl;
    private updateUiListener uilistener;//ui回调接口

    private DownloadListener listener = new DownloadListener() {
        @Override
        public void onProgress(int progress) {
            getNotificationManager().notify(1, getNotification("Downloading...", progress));
            uilistener.onUiProgress(progress);
        }

        @Override
        public void onSuccess() {
            downloadTask = null;
            stopForeground(true);
            getNotificationManager().notify(1, getNotification("Download Success", -1));
            Toast.makeText(com.example.api.DownloadService.this, "Download Success", Toast.LENGTH_SHORT).show();

            /*下载完毕后自动打开*/
            String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/"));
            String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
            File file = new File(directory + fileName);
            Log.d("file", directory + " " + fileName);


            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri uri = FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".provider", file);
            intent.setDataAndType(uri, "application/vnd.android.package-archive");
            startActivity(intent);

        }

        @Override
        public void onFailed() {
            downloadTask = null;
            stopForeground(true);
            getNotificationManager().notify(1, getNotification("Download Failed", -1));
            Toast.makeText(com.example.api.DownloadService.this, "Download Failed", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPause() {
            downloadTask = null;//注意没有取消前台任务
            Toast.makeText(com.example.api.DownloadService.this, "Download Paused", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCanceled() {
            downloadTask = null;
            stopForeground(true);
            Toast.makeText(com.example.api.DownloadService.this, "Download Canceled", Toast.LENGTH_SHORT).show();
        }
    };

    //服务中发送前台服务通知
    private Notification getNotification(String title, int progress) {

        //8.0版本以上
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //开辟一个通道
            NotificationChannel mChannel;
            mChannel = new NotificationChannel("123", "name", NotificationManager.IMPORTANCE_LOW);
            getNotificationManager().createNotificationChannel(mChannel);

            Intent intent = new Intent(this, DownLoadActivity.class);
            PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);
            //NotificationCompat.Builder builder=new NotificationCompat.Builder(this);
            Notification.Builder builder = new Notification.Builder(this);
            builder.setSmallIcon(R.mipmap.ic_launcher);
            builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
            builder.setContentIntent(pi);
            builder.setContentTitle(title);
            builder.setChannelId("123");

            if (progress > 0) {
                builder.setContentText(progress + "%");//progress由子线程调用回调接口中的方法传来
                builder.setProgress(100, progress, false);
            }
            return builder.build();
        }//8.0版本以下
        else {
            Intent intent = new Intent(this, DownLoadActivity.class);
            PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);
            //NotificationCompat.Builder builder=new NotificationCompat.Builder(this);
            Notification.Builder builder = new Notification.Builder(this);
            builder.setSmallIcon(R.mipmap.ic_launcher);
            builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
            builder.setContentIntent(pi);
            builder.setContentTitle(title);
            if (progress > 0) {
                builder.setContentText(progress + "%");
                builder.setProgress(100, progress, false);
            }
            return builder.build();
        }

    }

    private NotificationManager getNotificationManager() {
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);//多次调用仍是同一个？
/*        NotificationManager是一个Android系统服务，用于管理和运行所有通知。NotificationManager不能被实例化，为了把Notification传给它，
        你可以用getSystemService()方法获取一个NotificationManager的引用。在需要通知用户时再调用notify()方法将Notification对象传给它。*/
    }

    public DownloadBinder mBinder = new DownloadBinder();

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return mBinder;
    }

    public class DownloadBinder extends Binder {
        public void startDownload(String url) {
            if (downloadTask == null) {
                downloadUrl = url;
                downloadTask = new DownloadTask(listener);
                downloadTask.execute(downloadUrl);
                Log.d("begin", "4,这里");
                startForeground(1, getNotification("Downloading...", 0));
                Log.d("begin", "5");
                Toast.makeText(com.example.api.DownloadService.this, "开始下载，下载进度可下拉状态栏查看", Toast.LENGTH_LONG).show();
                Log.d("begin", "6");
            }
        }

        public void setuilistener(updateUiListener listener) {
            uilistener = listener;
        }

        public void pauseDownload() {
            if (downloadTask != null) {
                downloadTask.pauseDownload();
            }
        }

        public void cancelDownload() {
            if (downloadTask != null) {
                downloadTask.cancelDownload();
            } else {
                if (downloadUrl != null) {
                    String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/"));
                    String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
                    File file = new File(directory + fileName);
                    if (file.exists()) {
                        file.delete();
                    }
                    getNotificationManager().cancel(1);
                    stopForeground(true);
                    Toast.makeText(com.example.api.DownloadService.this, "Canceled", Toast.LENGTH_SHORT).show();
                }
            }
        }

    }
}