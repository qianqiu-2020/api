package com.example.api;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.api.ActivityCollector;


import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/*此活动为登录与注册，保存密码功能采用preference实现*/
public class LoginActivity extends BaseActivity {

    private EditText accountEdit;
    private EditText passwordEdit;
    private Button registerButton;
    private Button loginButton;
    private Button forgetButton;

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private CheckBox remeberPassword;
    private CheckBox autoLogin;
    private TextView status;
    private TextView selfstatus;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("销毁", "login");
    }

    @Override
    protected void onStop() {
        super.onStop();
        //退出前安全关闭线程
        //tcp_sender.onWork=false;
        //tcp_sender_tread.interrupt();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
/*        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null) actionBar.hide();*/
        accountEdit = (EditText) findViewById(R.id.account);
        passwordEdit = (EditText) findViewById(R.id.password);
        registerButton = (Button) findViewById(R.id.register);
        loginButton = (Button) findViewById(R.id.login);
        forgetButton = (Button) findViewById(R.id.forget);
        pref = getSharedPreferences("login", Context.MODE_PRIVATE);
        remeberPassword = (CheckBox) findViewById(R.id.remember_pass);
        autoLogin = (CheckBox) findViewById(R.id.auto_login);
        status = (TextView) findViewById(R.id.status);
        selfstatus = (TextView) findViewById(R.id.selfstatus);
        getSupportActionBar().hide();
        boolean isRemember = pref.getBoolean("remember_password", false);
        boolean isAutoLogin = pref.getBoolean("auto_login", false);
        //获取读写权限
        if (ContextCompat.checkSelfPermission(com.example.api.LoginActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(com.example.api.LoginActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
        if(ContextCompat.checkSelfPermission(com.example.api.LoginActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(com.example.api.LoginActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
        }
        if (isRemember) {
            String account = pref.getString("account", "");
            String password = pref.getString("password", "");
            accountEdit.setText(account);
            passwordEdit.setText(password);
            remeberPassword.setChecked(true);
            Log.d("local", "检测到记住密码已勾选");
        }
        if (isAutoLogin) {
            String account = pref.getString("account", "");
            String password = pref.getString("password", "");
            accountEdit.setText(account);
            passwordEdit.setText(password);
            remeberPassword.setChecked(true);
            autoLogin.setChecked(true);
            //向服务器验证并跳转至首页
//            try {
//                login(account, password);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            if(token2!="null")
//            {
//                Intent intent = new Intent(ActivityCollector.activities.get(ActivityCollector.activities.size() - 1), MainActivity.class);
//                startActivity(intent);
//            }else{
//                editor.putBoolean("auto_login", false);
//                editor.apply();
//            }

        }



        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(ActivityCollector.activities.get(ActivityCollector.activities.size() - 1), RegisterActivity.class);
//                startActivity(intent);
            }
        });


        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String account = accountEdit.getText().toString();
                String password = passwordEdit.getText().toString();

                if (account.isEmpty() || password.isEmpty()) {
                    Toast.makeText(ActivityCollector.activities.get(ActivityCollector.activities.size() - 1), "账号或密码为空！", Toast.LENGTH_SHORT).show();
                    return;
                }
                /*存入preference的方法*/
                editor = pref.edit();


                if (autoLogin.isChecked()) {
                    editor.putBoolean("auto_login", true);
                    editor.putBoolean("remember_password", true);
                    editor.putString("account", account);
                    editor.putString("password", password);
                }else{
                    if (remeberPassword.isChecked()) {
                        editor.putBoolean("auto_login", false);
                        editor.putBoolean("remember_password", true);
                        editor.putString("account", account);
                        editor.putString("password", password);
                        Log.d("local", "将要存储的账号" + account + "密码" + password);
                    } else {
                        editor.putBoolean("remember_password", false);
                        editor.putBoolean("auto_login", false);
                    }
                }
                //SharedPreferences.Editor.clear()方法是把之前commit后保存的所有信息全部进行清空，同一次commit进行的操作如果含有clear()操作，则先执行clear()再执行其他，与代码前后顺序没有关系。
                editor.clear();
                editor.apply();
                Log.d("local", "apply，已存入preference");
                try {
                    login(account, password);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(token2!="null")//正确登录
                {
                    Intent intent = new Intent(ActivityCollector.activities.get(ActivityCollector.activities.size() - 1), MainActivity.class);
                    startActivity(intent);
                    finish();
                }else{
                    Toast.makeText(ActivityCollector.activities.get(ActivityCollector.activities.size()-1),"账号或密码错误！",Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    private void login(String user, String password) throws InterruptedException {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //http连接需要放到子线程中进行请求
                    URL httpUrl = new URL("http://api.sybapi.cc/api/user/login");
                    HttpURLConnection conn = (HttpURLConnection) httpUrl.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);//设置是否向HttpUrlConnction输出，因为这个是POST请求，参数要放在http正文内，因此需要设为true，默认情况下是false
                    conn.setDoInput(true);//设置是否向HttpUrlConnection读入，默认情况下是true
                    conn.setUseCaches(false);//POST请求不能使用缓存（POST不能被缓存）
                    conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");//设置请求头

                    conn.connect();//connect()函数会根据HttpURLConnection对象的配置值 生成http头部信息，因此在调用connect函数之前，就必须把所有的配置准备好


                    //正文的内容是通过outputStream流写入的，实际上outputStream不是一个网络流，充其量是个字符串流，往里面写入的东西不会立即发送到网络，
                    //而是存在于内存缓冲区中，待outputStream流关闭时，根据输入的内容生成http正文。至此，http请求的东西已经全部准备就绪
                    DataOutputStream dataOutputStream = new DataOutputStream(conn.getOutputStream());
                    JSONObject obj = new JSONObject();
                    obj.put("email", user);
                    obj.put("password", password);
                    byte[] t = obj.toString().getBytes("utf-8");
                    dataOutputStream.write(t);
                    dataOutputStream.flush();
                    dataOutputStream.close();

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
                            token2 = resultJson.getString("result");
                            token = "Bearer " + resultJson.getString("result");
                        }else {//密码错误
                            token="null";
                            token2="null";
                        }


                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
        t.start();
        t.join();
    }

}