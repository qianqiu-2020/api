package com.example.api;
/*聊天对象类，记录好友相关信息
* 如昵称，头像等...
* */
public class ImageInformation {
    private String name;
    private String url;

    public String getUrl() {
        return url;
    }
    public ImageInformation(String name, String url)
    {
        this.name=name;
        this.url=url;
    }
    public String getName()
    {
        return name;
    }

}
