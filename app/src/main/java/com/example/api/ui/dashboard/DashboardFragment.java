package com.example.api.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.api.ApiInformation;
import com.example.api.ApiListAdapter;
import com.example.api.ImagesActivity;
import com.example.api.R;

import java.util.ArrayList;
import java.util.List;

public class DashboardFragment extends Fragment {
    ApiListAdapter adapter;
    List<ApiInformation> apiList=new ArrayList<>();
    ListView listView;
    ImageView imageView;
    private DashboardViewModel dashboardViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);


        adapter=new ApiListAdapter(getActivity(),R.layout.api_item ,apiList);//实例化适配器
        listView=(ListView)root.findViewById(R.id.list_view);//获取listview实例
        listView.setAdapter(adapter);//给listview设置适配器
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ApiInformation apiinfo=apiList.get(position);//获取对应项内容
                Toast.makeText(getActivity(),apiinfo.getName(),Toast.LENGTH_SHORT).show();

                ImagesActivity.actionStart(getActivity(),apiinfo.getUrl_header(),apiinfo.getUrl_parameter());
            }
        });//每一项被点击时执行的操作

        apiList.clear();
        ApiInformation api1=new ApiInformation("二次元图库",R.drawable.image_1,"http://api.sybapi.cc/api/acg?key=","&type=json");
        apiList.add(api1);
        ApiInformation api2=new ApiInformation("小姐姐图库",R.drawable.image_2,"http://api.sybapi.cc/api/girls?key=","&type=json");
        apiList.add(api2);
        ApiInformation api3=new ApiInformation("随机头像图库",R.drawable.image_3,"http://api.sybapi.cc/api/avatar\\_random?key=","");
        apiList.add(api3);


         dashboardViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                //textView.setText(s);
            }
        });

        return root;
    }
}