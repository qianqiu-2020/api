package com.example.api;
/*聊天对象类，记录好友相关信息
* 如昵称，头像等...
* */
public class ApiInformation {
    private String name;//api的名字
    private int imageId;//api列表中显示的功能图标
    private String url_header;//url的开头部分
    private String url_parameter;//key之后的参数
    public ApiInformation(String name, int imageId,String url_header,String url_parameter)
    {
        this.name=name;
        this.imageId=imageId;
        this.url_header=url_header;
        this.url_parameter=url_parameter;
    }

    public String getName()
    {
        return name;
    }
    public int getImageId()
    {
        return imageId;
    }

    public String getUrl_header() {
        return url_header;
    }

    public String getUrl_parameter() {
        return url_parameter;
    }
}
