package com.example.mt.spvjar.bean;

import android.content.Intent;

public class EventBusBean {
    private int code;
    private String msg;
    private Intent intent;

    public EventBusBean(int code, String msg,Intent intent) {
        this.code = code;
        this.msg = msg;
        this.intent = intent;
    }

    public Intent getIntent() {
        return intent;
    }

    public void setIntent(Intent intent) {
        this.intent = intent;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
