package com.example.mt.spvjar.broadcast;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.mt.spvjar.MainActivity;
import com.example.mt.spvjar.bean.EventBusBean;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

public class MyReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        String TAG = "MyReceiver";
        //获取MainActivity的componentName
        ComponentName cmpName = new ComponentName(context, "com.example.mt.spvjar.MainActivity");
        if (cmpName != null) {
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningTaskInfo> taskInfoList = am.getRunningTasks(50);
            Log.e(TAG, "cmpName: " + cmpName);
            boolean flags = false;
            //在栈中查找是否存在MainActivity的实例
            for (ActivityManager.RunningTaskInfo runningTaskInfo : taskInfoList) {
                if (runningTaskInfo.baseActivity.equals(cmpName)) {
                    Log.e(TAG, "id: " + runningTaskInfo.id);
                    Log.e(TAG, "description: " + runningTaskInfo.description);
                    Log.e(TAG, "number of activities: " + runningTaskInfo.numActivities);
                    Log.e(TAG, "topActivity: " + runningTaskInfo.topActivity);
                    Log.e(TAG, "baseActivity: " + runningTaskInfo.baseActivity.toString());
                    flags = true;
                    break;
                }
            }
            if (flags)
                //请求分发到已生成的activity中
                EventBus.getDefault().post(new EventBusBean(100, "sendBroadCast success", intent));
            else {
                //app被系统或者被手动清理了的时候，重新启动app
                Intent intent1 = new Intent(context, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent1);
            }
        }


    }

}
