package com.example.api;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.example.api.ui.home.HomeFragment;
import com.example.api.ui.home.HomeViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkVersion();//检查版本

        BottomNavigationView navView = findViewById(R.id.nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        //使状态栏标题随当前碎片改变而改变，我们不需要状态栏，故也不需要设置此项
        //NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
        getSupportActionBar().hide();//隐藏工具栏

        HomeViewModel viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        viewModel.setToken(token2);//共享数据给网页所在fragment

        getKey();//获取用户key值，前提是已经登录

    }

    @Override
    protected void onStop() {
        super.onStop();
    }
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("debug", "主活动被销毁");
    }

    private void getKey() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //http连接需要放到子线程中进行请求
                    URL httpUrl = new URL("http://api.sybapi.cc/api/user/getUserInfo");
                    HttpURLConnection conn = (HttpURLConnection) httpUrl.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);//设置是否向HttpUrlConnction输出，因为这个是POST请求，参数要放在http正文内，因此需要设为true，默认情况下是false
                    conn.setDoInput(true);//设置是否向HttpUrlConnection读入，默认情况下是true
                    conn.setUseCaches(false);//POST请求不能使用缓存（POST不能被缓存）
                    conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");//设置请求头
                    conn.setRequestProperty("Authorization", token);//设置请求头

                    conn.connect();//connect()函数会根据HttpURLConnection对象的配置值 生成http头部信息，因此在调用connect函数之前，就必须把所有的配置准备好

                    //正文的内容是通过outputStream流写入的，实际上outputStream不是一个网络流，充其量是个字符串流，往里面写入的东西不会立即发送到网络，
                    //而是存在于内存缓冲区中，待outputStream流关闭时，根据输入的内容生成http正文。至此，http请求的东西已经全部准备就绪
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

                            key = result.getString("key");
                        } else {//密码错误
                            key = "null";
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();
    }


    private void checkVersion() {
        double currentVesion = 1.0f;//设置当前版本
        /*判断版本*/
        //如果版本小于最新版本，则跳转至下载活动
        try {
            // 配置文件所在地址
            URL url = new URL("https://app.sybapi.cc/updateConfiguration.json");

            // Read all the text returned by the server
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String str = new String();
                        String temp;
                        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                        while ((temp = in.readLine()) != null) {
                            str += temp;
                            // str is one line of text; readLine() strips the newline character(s)
                        }
                        in.close();
                        Log.d("debug", str);

                        JSONObject obj = new JSONObject(str);
                        int type = obj.getInt("type");
                        double version = obj.getDouble("version");
                        String content = obj.getString("content");
                        String address = obj.getString("address");
                        Log.d("debug最新版本", String.valueOf(version));
                        Log.d("debug更新内容", content);
                        Log.d("debug下载地址", address);
                        if (version - currentVesion > 0.0001)//跳转至更新活动
                        {
                            Intent intent = new Intent(MainActivity.this, DownLoadActivity.class);
                            intent.putExtra("version", String.valueOf(version));
                            intent.putExtra("address", address);
                            intent.putExtra("content", content);
                            startActivity(intent);
                            finish();//旧版本更新时不允许使用，故销毁主活动
                        } else ;//已是最新版本，什么也不需要做
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

}