package com.example.api;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ImagesActivity extends BaseActivity {
    String url_header;
    String url_parameter;
    private List<ImageInformation> imageList = new ArrayList<>();
    private ImagesAdapter adapter;
    RecyclerView recyclerView;

    public static void actionStart(Context context,String url_header,String url_parameter)
    {
        Intent intent=new Intent(context,ImagesActivity.class);
        intent.putExtra("url_header",url_header);
        intent.putExtra("url_parameter",url_parameter);
        context.startActivity(intent);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_images);

        Intent intent=getIntent();
        url_header=intent.getStringExtra("url_header");
        url_parameter=intent.getStringExtra("url_parameter");

        getSupportActionBar().hide();
        try {
            initImages();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        recyclerView = (RecyclerView) findViewById(R.id.images_recycler_view);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 1);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new ImagesAdapter(imageList);
        recyclerView.setAdapter(adapter);

    }

    private void initImages() throws InterruptedException {
        if (key != "null") {
            imageList.clear();
            t.start();
        }
    }

    Thread t = new Thread(new Runnable() {
        @Override
        public void run() {
            for(int i=0;i<10;i++) {
                try {
                    //http连接需要放到子线程中进行请求
                    URL httpUrl = new URL(url_header + key + url_parameter);
                    HttpURLConnection conn = (HttpURLConnection) httpUrl.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setUseCaches(false);//POST请求不能使用缓存（POST不能被缓存）
                    conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");//设置请求头
                    conn.connect();//connect()函数会根据HttpURLConnection对象的配置值 生成http头部信息，因此在调用connect函数之前，就必须把所有的配置准备好

                    //对outputStream的写操作，又必须要在inputStream的读操作之前
                    InputStream inputStream = conn.getInputStream();// <===注意，实际发送请求的代码段就在这里

                    if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {

                        StringBuilder sb = new StringBuilder();
                        //面对获取的输入流进行读取
                        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                        String line;
                        while ((line = br.readLine()) != null) {
                            sb.append(line);
                        }
                        JSONObject resultJson = new JSONObject(sb.toString());//把String对象转为JSONObject对象
                        if (resultJson.getInt("status") == 1) {
                            JSONObject result = resultJson.getJSONObject("result");
                            ImageInformation image = new ImageInformation("test" +  String.valueOf(i), result.getString("url"));
                            imageList.add(image);
                            adapter.notifyItemInserted(imageList.size() - 1);//更新适配器，通知适配器消息列表有新的数据插入
                            recyclerView.scrollToPosition(imageList.size() - 1);//显示最新的消息，定位到最后一行

                        } else {//密码错误

                        }


                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    });

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            //Intent intent = new Intent(ImagesActivity.this,MainActivity.class);
            //startActivity(intent);
            this.finish();
        }
        return super.onKeyDown(keyCode, event);
    }
}