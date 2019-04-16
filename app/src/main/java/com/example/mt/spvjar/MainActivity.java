package com.example.mt.spvjar;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mt.spvjar.bean.EventBusBean;
import com.example.mt.spvjar.bean.FloatWindowItemBean;
import com.example.mt.spvjar.scheme.SchemeIntent;
import com.example.mt.spvjar.view.FloatDialogWindow;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    FloatDialogWindow floatDialogWindow;

    private SharedPreferences sharedPreferences;
    private boolean notShow  =  true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EventBus.getDefault().register(this);
        Button button = findViewById(R.id.button1);
        Button changeColor = findViewById(R.id.change_color);
        final TextView textView = findViewById(R.id.textview);
        Button pinset = findViewById(R.id.pinset);
        pinset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SchemeIntent schemeIntent = new SchemeIntent(MainActivity.this);
                schemeIntent.GoToOtherApp("mra://pinset");
            }
        });
        Button search = findViewById(R.id.seach_wynn_id);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SchemeIntent schemeIntent = new SchemeIntent(MainActivity.this);
                schemeIntent.GoToOtherApp("mra://index");
            }
        });
        Button reprint = findViewById(R.id.reprint);
        reprint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SchemeIntent schemeIntent = new SchemeIntent(MainActivity.this);
                schemeIntent.GoToOtherApp("mra://reprint");
            }
        });
//        CommonAlertDialog.Builder builder = new CommonAlertDialog.Builder(this);
//        builder.setPositiveButtonBackground(R.drawable.backg);
//        builder.setTitle("请求设置");
//        builder.setMessage("开启悬浮窗，需要去系统设置");
//        builder.setBgCorner(new int[]{30,30,30,30});
//        builder.setNegativeButton(new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
//            }
//        });
//        builder.setPositiveButton(new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                //请求权限
//                Intent permessionIntent = new Intent("android.settings.action.MANAGE_OVERLAY_PERMISSION",Uri.parse("package:"+MainActivity.this.getPackageName()));
//                startActivityForResult(permessionIntent,100);
//                dialog.dismiss();
//            }
//        });
//        builder.create().show();
        Context otherAppsContext = null;

        try{
            otherAppsContext = createPackageContext("com.wynn.mma",Context.CONTEXT_IGNORE_SECURITY);
        }catch (PackageManager.NameNotFoundException e){
            e.printStackTrace();
        }
        sharedPreferences = otherAppsContext.getSharedPreferences("mma_data",Context.MODE_MULTI_PROCESS);
        Toast.makeText(this,sharedPreferences.getString("oa_token","nothing"),Toast.LENGTH_LONG).show();
        changeColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SchemeIntent schemeIntent = new SchemeIntent(getApplicationContext());
                schemeIntent.backToMMA("mma://fuckyou");
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri userUri = Uri.parse("content://com.wynn/user");

                Cursor c = getContentResolver().query(userUri,null,null,null,null);
                Log.i("woshiniba",""+c.getCount());
                String oa_token = "";
                while (c.moveToNext()){
                    if(c.getString(c.getColumnIndex("keyName")).equals("oa_token")){
                        oa_token = c.getString(c.getColumnIndex("keyValue"));
                        break;
                    }
                    Log.i("woshiniba",c.getString(c.getColumnIndex("keyName"))+"  "+c.getString(c.getColumnIndex("keyValue")));
                }
                Toast.makeText(MainActivity.this,""+oa_token,Toast.LENGTH_LONG).show();
            }
        });
        floatDialogWindow = FloatDialogWindow.getInstance(MainActivity.this);
        floatDialogWindow.hideWindow();
        floatDialogWindow.setBtnImg(R.drawable.ic_launcher_background, new FloatDialogWindow.FloatWindowOnClickListener() {
            @Override
            public void onBtnClick() {

                backToMMA();
            }
        });
        //设置显示和消失的动画特效
        floatDialogWindow.setWindowAnimationStyle(R.style.default_style);
        final List<FloatWindowItemBean> appList= new ArrayList<>();
        appList.add(new FloatWindowItemBean("imt://1",R.drawable.w1));
        appList.add(new FloatWindowItemBean("imt://2",R.drawable.w2));
        appList.add(new FloatWindowItemBean("imt://3",R.drawable.w3));
        appList.add(new FloatWindowItemBean("imt://4",R.drawable.w2));
        appList.add(new FloatWindowItemBean("imt://5",R.drawable.w1));
        floatDialogWindow.setItem(appList);
    }
    @Subscribe
    public void onEvent(EventBusBean eventBusBean){
        Log.i(TAG,eventBusBean.getMsg());
        ActivityManager activityManager = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> taskInfos = activityManager.getRunningTasks(50);
        for(ActivityManager.RunningTaskInfo a : taskInfos){
            if(a.topActivity.getPackageName().equals("com.example.mt.spvjar")){
                activityManager.moveTaskToFront(a.id,0);
                Log.i(TAG,"move it to front");
                return;
            }
        }
       dealSchemeRequest(eventBusBean);
    }

    /**
     * 根据广播传递过来的跳转信息，分析scheme中的scheme和host,判断跳转的页面，通知vue跳转界面
     * @param eventBusBean 消息对象
     */
    public void dealSchemeRequest(EventBusBean eventBusBean){
        //拉起应用成功后，还要根据intent跳转到指定的html页面
        Intent a = eventBusBean.getIntent();
        //获取scheme
        Uri scheme = a.getData();
        String scheme_pro = scheme.getScheme();
        String scheme_host =  scheme.getHost();
        Log.i(TAG,"scheme="+scheme_pro);
        Log.i(TAG,"host="+scheme_host);
        //判断是否scheme是否等于mma
        if(scheme_pro.equals("mma")){
            //根据host对判断需要跳转到那个页面，然后native通知Vue进行跳转
            switch (scheme_host){
                case "main":
                    //TODO 通知js跳转页面
                    break;
                case "search":
                    break;
            }
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        if(notShow){
            notShow = false;
            return;
        }
        floatDialogWindow.hideWindow();

    }


    @Override
    protected void onPause() {
        super.onPause();

        floatDialogWindow.showWindow(60,60,30,100);
    }
    public void backToMMA(){
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        activityManager.moveTaskToFront(getTaskId(), ActivityManager.MOVE_TASK_WITH_HOME);
    }
}
