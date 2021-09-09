package com.example.api;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;



import java.util.List;

/*好友列表填充所用的适配器*/
public class ApiListAdapter extends ArrayAdapter<ApiInformation> {
    private int resourceId;//每一项所用的布局，在实例化适配器时传入
    public ApiListAdapter(Context context, int textViewRourceId, List<ApiInformation> objects){
        super(context,textViewRourceId,objects);
        resourceId=textViewRourceId;
    }
    /*listview中每一项将要可见时，调用此方法加载内容，此方法调用一次加载一项的内容*/
    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        ApiInformation apiinfo=getItem(position);//获取适配器对应的List(即构造函数中传入的objects)中的一项数据
        View view;//listview中一项的模板，注释掉的内容为采用固定数量的模板，而当前则是每一项都新造一个模板
//        if(convertView==null){
        view= LayoutInflater.from(getContext()).inflate(resourceId,parent,false);
        Log.d("mark1", String.valueOf(view.hashCode()));
  /*      }
        else {
            view=convertView;
            Log.d("mark2", String.valueOf(view.hashCode()));
        }*/

        /*获取每一项中的控件*/
        ImageView apiImage=(ImageView)view.findViewById(R.id.friend_image);
        TextView apiName=(TextView)view.findViewById(R.id.friend_name);
        //EditText editText=(EditText)view.findViewById(R.id.edit_text);
        /*把从适配器对应的List中取到的数据设置到控件中*/
        apiImage.setImageResource(apiinfo.getImageId());
        apiName.setText(apiinfo.getName());
        //editText.setText(friend.getName());

        return view;
    }

}
