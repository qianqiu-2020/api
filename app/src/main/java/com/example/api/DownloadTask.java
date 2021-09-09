package com.example.api;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

//异步消息处理，采用AsyncTask实现
public class DownloadTask extends AsyncTask<String,Integer,Integer> {
    public static final int TYPE_SUCCESS=0;
    public static final int TYPE_FAILED=1;
    public static final int TYPE_PAUSED=2;
    public static final int TYPE_CANCELED=3;
    private DownloadListener listener;//回调接口
    private boolean isCanceled=false;
    private boolean isPaused=false;
    private int lastProgress;

    public DownloadTask(DownloadListener listener){
        this.listener=listener;
    }
    @Override
    protected Integer doInBackground(String... strings) {
        InputStream is = null;
        RandomAccessFile savedFile = null;
        File file = null;
        try {
            long downloadedLength=0; //记录已下载文件长度
            String downloadUrl=strings[0];
            String fileName=downloadUrl.substring(downloadUrl.lastIndexOf("/"));
            String directory= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
            file=new File(directory+fileName);
            if(file.exists()){
                downloadedLength=file.length();
            }
            long contentLength=getContentLength(downloadUrl);//获取源文件长度
            Log.d("progress获取到的文件总长为", String.valueOf(contentLength));

            if(contentLength==0){
                return TYPE_FAILED;
            }else if(contentLength==downloadedLength){
                return TYPE_SUCCESS;
            }
            OkHttpClient client=new OkHttpClient();
            Request request=new Request.Builder().addHeader("RANGE","bytes="+downloadedLength+"-")
                    .url(downloadUrl)
                    .build();
            Response response=client.newCall(request).execute();
            if(response!=null){
                is=response.body().byteStream();
                savedFile=new RandomAccessFile(file,"rw");
                savedFile.seek(downloadedLength);//跳过已下载的字节
                byte[] b=new byte[1024];
                int total=0;//本次下载量
                int len;
                while ((len=is.read(b))!=-1){
                    if(isCanceled){
                        return TYPE_CANCELED;
                    }else if(isPaused){
                        return TYPE_PAUSED;
                    }else {
                        total+=len;
                        savedFile.write(b,0,len);
                        int progress=(int)((total+downloadedLength)*100/contentLength);
                        Log.d("progress本次下载", String.valueOf(total));
                        Log.d("progress本次下载前本地已存储部分", String.valueOf(downloadedLength));
                        Log.d("progress总长", String.valueOf(contentLength));

                        Log.d("progress总进度", String.valueOf(progress));
                        publishProgress(progress);
                    }
                }
                response.body().close();
                return TYPE_SUCCESS;
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (savedFile != null) {
                    savedFile.close();
                }
                if (isCanceled && file != null) {
                    file.delete();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return TYPE_FAILED;
    }
    //后台任务doInBackground中调用publishProgress方法后会调用此方法进行一些ui操作，同时通过回调接口返回下载状态
    @Override
    protected void onProgressUpdate(Integer... values) {
        int progress=values[0];
        if(progress>lastProgress){
            listener.onProgress(progress);

            lastProgress=progress;
        }
    }
    //后台任务doInBackground执行完毕并通过return语句返回时，调用此方法，可进行ui操作，同时通过回调接口返回下载状态
    @Override
    protected void onPostExecute(Integer integer) {
        switch (integer){
            case TYPE_SUCCESS:
                listener.onSuccess();
                break;
            case TYPE_FAILED:
                listener.onFailed();
                break;
            case TYPE_PAUSED:
                listener.onPause();
                break;
            case TYPE_CANCELED:
                listener.onCanceled();
                break;
            default:
                break;
        }
    }

    public void pauseDownload(){
        isPaused=true;
    }

    public void cancelDownload() {
        isCanceled=true;
    }
    private long getContentLength(String downloadUrl)throws IOException{
        Log.d("testurl", downloadUrl);
        OkHttpClient client=new OkHttpClient();
        Request request=new Request.Builder()
                .url(downloadUrl)
                .addHeader("Accept-Encoding","identity")
                .build();
        Response response=client.newCall(request).execute();
        if(response!=null &&response.isSuccessful()){
            long contentLength=response.body().contentLength();
            System.out.println( contentLength );
            Log.d("test v", String.valueOf(response.body().contentLength()));
            return contentLength;
        }
        return 0;
    }
}
