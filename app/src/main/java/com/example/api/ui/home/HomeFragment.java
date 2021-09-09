package com.example.api.ui.home;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.api.R;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    public WebView webView;

//    public String token=getActivity().getToken();
    public String token2;

    int n=0;
    public String url="http://user.sybapi.cc/#/login";
    long exitTime;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider(getActivity()).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        token2=homeViewModel.getToken();
        //让TextView观察ViewModel中数据的变化,并实时展示
        homeViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                //textView.setText(s);
            }
        });

        //暂时设置为网页
        webView = (WebView) root.findViewById((R.id.web_view));
        initView(webView);

        if (homeViewModel.webViewState != null) {
            //Fragment实例并未被销毁, 重新create view
            webView.restoreState(homeViewModel.webViewState);
          //  webView.clearCache(true);
            webView.loadUrl("javascript: localStorage.setItem('token', '"+token2+"');");
            Log.d("debug", "未销毁,读取webview状态");
        } else if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState);
            Log.d("debug", "restoreState");
        } else {
          //  webView.clearCache(true);
            webView.loadUrl("javascript: localStorage.setItem('token', '"+token2+"');");
            webView.loadUrl("http://user.sybapi.cc/#/dashboard");
            Log.d("debug", "firstload");
        }




        // This callback will only be called when MyFragment is at least Started.
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                // Handle the back button event
                if (webView.canGoBack())
                    webView.goBack();// 返回前一个页面
                else if ((System.currentTimeMillis() - exitTime) > 2000) {
                    Toast.makeText(getActivity(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
                    exitTime = System.currentTimeMillis();
                } else {
                    getActivity().finish();
                    System.exit(0);
                }
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);
        return root;
    }
    public void initView(WebView webView){
        WebSettings webSettings = webView.getSettings();
        webView.setVerticalScrollBarEnabled(false); //垂直不显示
        webView.setHorizontalScrollBarEnabled(false);//水平滑动条不显示
        webSettings.setJavaScriptEnabled(true);
        webSettings.setBuiltInZoomControls(true); // 显示放大缩小
        webSettings.setSupportZoom(true); // 可以缩放
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);

        //设置 localStorge 传递信息到服务器的设置 默认是关闭的
        webSettings.setDomStorageEnabled(true);// 打开本地缓存提供JS调用,至关重要
        webSettings.setAppCacheMaxSize(1024 * 1024 * 8);// 实现8倍缓存
        webSettings.setAllowFileAccess(true);
        webSettings.setAppCacheEnabled(true);
        String appCachePath = getActivity().getApplication().getCacheDir().getAbsolutePath();
        webSettings.setAppCachePath(appCachePath);
        webSettings.setDatabaseEnabled(true);
        webSettings.setDomStorageEnabled(true);//设置支持html5本地存储，有些h5页面服务器做了缓存，webview控件也要设置，否则显示不出来页面           webSettings.setSupportMultipleWindows(true);//支持多窗口
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        webSettings.setDefaultTextEncodingName("UTF-8");
        webSettings.setAllowFileAccess(true);// 设置可以访问缓存文件
        webSettings.setAppCacheEnabled(true);//应用可以有缓存
        webSettings.setDefaultZoom(WebSettings.ZoomDensity.MEDIUM);
        webView.setWebViewClient(new WebViewClient() {


            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {

                Map<String,String> header = new HashMap<String, String>();;
//                webView.loadUrl("javascript: localStorage.setItem('token', '"+token2+"');");
//                header.put("Authorization",token);
                view.loadUrl(url);//采用webview控件在应用内打开其他链接
                return super.shouldOverrideUrlLoading(view, request);
            }
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request)
            {

                return null;
            }
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
//                if(url=="http://user.sybapi.cc/#/dashboard"||url=="http://user.sybapi.cc/#/login")
//                {
                    webView.loadUrl("javascript: localStorage.setItem('token', '"+token2+"');");
//                }
                //不太好的用法，但删去则会出现未记住登录状态
//                webView.loadUrl("javascript: localStorage.setItem('token', '"+token2+"');");

        }

        });
    };

    private void getCookie(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //http连接需要放到子线程中进行请求
                    URL httpUrl = new URL(url);
                    HttpURLConnection conn = (HttpURLConnection) httpUrl.openConnection();
                    conn.setRequestMethod("GET");

                    conn.connect();
                    if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        String response = conn.toString();//在conn中会有cookie信息，如下图
                        String[] connRespnse = response.split(";");
                        if (connRespnse != null && connRespnse.length > 1) {
                            synCookies(connRespnse[1]);
                        }
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                webView.loadUrl(url);//webview控件调用需要在主线程中进行
                            }
                        });


                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }

    private void synCookies(String cookie){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager.createInstance(getContext());
        }
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setCookie(url, cookie);//如果没有特殊需求，这里只需要将session id以"key=value"形式作为cookie即可
    }

    @Override
    public void onStop() {
        super.onStop();

        //Fragment不被销毁(Fragment被加入back stack)的情况下, 依靠Fragment中的成员变量保存WebView状态
        if (homeViewModel.webViewState == null) {
            homeViewModel.initWebViewState();
            Log.d("debug", "初始化webViewState");
        }
        webView.saveState(homeViewModel.webViewState);
        Log.d("debug", "onStop");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
        Log.d("debug", "onSaveInstanceState");
    }
    public void refresh() {
    }
}
