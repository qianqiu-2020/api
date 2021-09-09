package com.example.api;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.webkit.WebView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.api.ui.home.HomeViewModel;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/*
此活动继承自AppCompatActivity
所有活动再继承自此抽象活动
在此活动中写一些所有活动都需要用到的功能，如网络线程
*/
public abstract class BaseActivity extends AppCompatActivity {
    private HomeViewModel homeViewModel;
    protected static String key;
    protected static String token;
    protected static String token2;
    int n=0;
    public String url="http://user.sybapi.cc/";
    long exitTime;

   /*每个活动创建时，加入到ActivityCollector中*/
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCollector.addActivity(this);
    }
    /*每个活动销毁时，从ActivityCollector中删除*/
    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }
//    public abstract void processMessage(Message msg);//在handle中调用此方法，处理handle收到的数据，每个活动都有且需实现的方法
//    //静态的变量，多个子类可以共享使用！
//    public static TCP_Sender tcp_sender=new TCP_Sender();
//    public static UDP_Sender udp_sender=new UDP_Sender();
//
//    public static TCP_Listener tcp_listener=new TCP_Listener();
//    public static UDP_Listener udp_listener=new UDP_Listener();
//    public static Thread tcp_sender_tread;//发送tcp消息
//    public static Thread tcp_listener_tread;//接收tcp消息
//    public static Thread udp_sender_tread;//发送udp消息
//    public static Thread udp_listener_tread;//接收udp消息

}
