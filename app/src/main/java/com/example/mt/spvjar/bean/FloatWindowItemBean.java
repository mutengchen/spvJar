package com.example.mt.spvjar.bean;

public class FloatWindowItemBean {
    private String url;
    private int img;

    public FloatWindowItemBean(String url, int img) {
        this.url = url;
        this.img = img;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getImg() {
        return img;
    }

    public void setImg(int img) {
        this.img = img;
    }
}
