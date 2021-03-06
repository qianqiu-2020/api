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
                    //http??????????????????????????????????????????
                    URL httpUrl = new URL(url_header + key + url_parameter);
                    HttpURLConnection conn = (HttpURLConnection) httpUrl.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setUseCaches(false);//POST???????????????????????????POST??????????????????
                    conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");//???????????????
                    conn.connect();//connect()???????????????HttpURLConnection?????????????????? ??????http??????????????????????????????connect???????????????????????????????????????????????????

                    //???outputStream??????????????????????????????inputStream??????????????????
                    InputStream inputStream = conn.getInputStream();// <===???????????????????????????????????????????????????

                    if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {

                        StringBuilder sb = new StringBuilder();
                        //????????????????????????????????????
                        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                        String line;
                        while ((line = br.readLine()) != null) {
                            sb.append(line);
                        }
                        JSONObject resultJson = new JSONObject(sb.toString());//???String????????????JSONObject??????
                        if (resultJson.getInt("status") == 1) {
                            JSONObject result = resultJson.getJSONObject("result");
                            ImageInformation image = new ImageInformation("test" +  String.valueOf(i), result.getString("url"));
                            imageList.add(image);
                            adapter.notifyItemInserted(imageList.size() - 1);//??????????????????????????????????????????????????????????????????
                            recyclerView.scrollToPosition(imageList.size() - 1);//?????????????????????????????????????????????

                        } else {//????????????

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