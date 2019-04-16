package com.example.mt.spvjar.scheme;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.example.mt.spvjar.R;

import java.util.List;

public class SchemeIntent {
    private Context context;
    private String TAG = "SchemeIntent";
    private static final String PACKAGENAME = "com.wynn.mma";
    private static final String RECEIVERNAME = PACKAGENAME+".common.receiver.AppInfoReceiver";
    public SchemeIntent(Context context) {
        this.context = context;
    }

    /**
     * app之间scheme协议之间的跳转
     * @param scheme app应用的scheme
     */
    public void GoToOtherApp( String scheme){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(scheme));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //需要判断是否存在该指定scheme的app，如果不存在的话提示app没有安装，如果存在的话，跳转
        isActivityExist(intent);
    }
    private void isActivityExist(Intent intent){
        if(context.getPackageManager().resolveActivity(intent,0)==null){
//            Toast.makeText(context,context.getResources().getString(R.string.scheme_app_not_install),Toast.LENGTH_LONG).show();
                        Toast.makeText(context,"not install Related app",Toast.LENGTH_LONG).show();

        }else{
            context.startActivity(intent);
        }
    }

    //携带信息返回mma
    public void backToMMA(String scheme){
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Context c = null;
        //创建mma的applicationContext，以防mma在后台太久之后，被系统杀死了，或者是手动的清除mma
        try {
            c = context.createPackageContext(PACKAGENAME, Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        //如果没有安装mma的话，不进行广播的发送
        if(c== null){
            //提示信息
//            Toast.makeText(context,context.getResources().getString(R.string.scheme_mma_not_install),Toast.LENGTH_LONG).show();
            Toast.makeText(context,"not installed mma app",Toast.LENGTH_LONG).show();

            return;
        }
        //设置广播的接受者
        intent.setClassName(c,RECEIVERNAME);
        //设置启动类型为兼容那些被杀死的app的receiver
        intent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        //跟mma的广播接收器的action匹配
        intent.setAction(PACKAGENAME);
        //检查scheme的格式是否正确,检查scheme,检查host,检查参数是否有违规符号
        if(!checkScheme(scheme)){
            Toast.makeText(context,"please check the scheme format",Toast.LENGTH_LONG).show();
            return;
        }
        intent.setData(Uri.parse(scheme));
        context.sendBroadcast(intent);
    }

    //判断当前是否scheme是否有效
    private boolean checkScheme(String url) {
        PackageManager packageManager = context.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
        boolean isValid = !activities.isEmpty();
        return isValid;
    }

    //向mma获取key对应的value
    private String getContent(String key){
        //暂时是获取sso_token
        Uri uri = Uri.parse("content://com.wynn/user");
        String key_value = "";
        //获取mma当前用户数据
        Cursor cursor = context.getContentResolver().query(uri,null,null,null,null);

        while(cursor.moveToNext()){
            if(cursor.getString(cursor.getColumnIndex("keyName")).equals("oa_token")){
                key_value = cursor.getString(cursor.getColumnIndex("keyValue"));
                break;
            }
            Log.i("keyValue", "keyValue = "+cursor.getString(cursor.getColumnIndex("keyValue")));


        }
        return key_value;
    }

}
