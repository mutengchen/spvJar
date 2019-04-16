package com.example.mt.spvjar.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.example.mt.spvjar.bean.FloatWindowItemBean;
import com.example.mt.spvjar.utils.DensityUtils;
import com.example.mt.spvjar.utils.StatusBarUtils;

import java.util.ArrayList;
import java.util.List;

public class FloatDialogWindow implements View.OnClickListener, View.OnTouchListener{
    private ImageView btn = null;
    private boolean isShow = false;
    private static WindowManager windowManager = null;

    private AnimatorSet mShowAnimatorSet, mHideAnimatorSet;
    private static FloatDialogWindow floatDialogWindow;
    private int windowAnimationStyle =  -1;
    private int[] itemAnimationStyle;
    private Context context;
    private List<FloatWindowItemBean> item;
    private List<ImageView> itemList;
    private WindowManager.LayoutParams params;
    DisplayMetrics displayMetrics;
    private int btnX = 0;
    private int btnY = 0;
    private long mLastTime;
    private long mCurrentTime;
    private float mStartX;
    private float mStartY;
    private float mEndY = -1;
    private float mEndX = -1;
    private float mTouchStartY;
    private float mTouchStartX;
    private int btnStatus = 0; //0 为正常状态，1表示休眠状态，2表示展开子button状态
    private float initX;
    private float initY;
    private int initViewWidth;
    private int initViewHeight;
    private boolean itemIsMoveHide = false; //子button被move事件隐藏了
    private FloatItemOnClickListener floatItemOnClickListener;
    private FloatWindowOnClickListener floatWindowOnClickListener;
    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if(mShowAnimatorSet!=null){
                Log.i("animationzz","zzz");
                mShowAnimatorSet.start();
            }
            else{
                Log.i("animationzz","null");
            }
//            mShowAnimatorSet.start();
            return false;
        }
    });
    private FloatDialogWindow(Context context) {
        this.context = context;
        this.windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        this.displayMetrics = new DisplayMetrics();
    }
    public static FloatDialogWindow getInstance(Context context){
        if(floatDialogWindow==null){
            Log.i("floatDialogWindow","zz");
            floatDialogWindow = new FloatDialogWindow(context);
        }
        return floatDialogWindow;
    }

    //初始化按钮并设置监听函数
    public void setBtnImg(int resId,FloatWindowOnClickListener floatWindowOnClickListener) {
//        View view = LayoutInflater.from(context).inflate(R.layout.float_button_img,null,false);
        this.btn = new ImageView(context);
        this.btn.setOnClickListener(this);
        this.btn.setOnTouchListener(this);
        this.btn.setImageResource(resId);
        this.floatWindowOnClickListener = floatWindowOnClickListener;
//        setAnimator();

    }

    public void setItem(List<FloatWindowItemBean> list) {
        this.item = list;
    }
    public void setFloatItemOnCLick(FloatItemOnClickListener floatItemOnCLick){
        this.floatItemOnClickListener = floatItemOnCLick;
    }


    //显示子app列表，如果有的话
    private void showItemApp() {
        //都是根据mma按钮的位置来做确定位置的
        if (this.btn == null)
            return;
        if(item==null)
            return;
        itemList = new ArrayList<>();
        //获取btn当前在窗体内的x,y
        Log.i("Flaot:width", "" + this.btn.getWidth());
        Log.i("Flaot:height", "" + this.btn.getHeight());
        for (int i = 0; i < this.item.size(); i++) {
            //暂时不能超过五个，超过五个的话布局，第六个及其后面的item的显示的位置是乱的
            if(i>4){
                return;
            }
            //创建imageView
            ImageView imageView = new ImageView(context);
            //设置监听对象
            imageView.setImageResource(this.item.get(i).getImg());
            imageView.setTag(imageView.getId(),""+i);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(floatItemOnClickListener!=null)
                    floatItemOnClickListener.onItemClick(Integer.parseInt((String) v.getTag(v.getId())));
                }
            });

            WindowManager.LayoutParams temp = new WindowManager.LayoutParams();
            double rangle = Math.toRadians(90-i*45);
            //btn与子button之间的距离
            double item_juli = DensityUtils.dip2px(context,70);
            //子button的长宽
            double item_r =  DensityUtils.dip2px(context,40);
            //判断当前按钮是在屏幕的左边
            //计算方法：及获取b
            if(btnX<displayMetrics.widthPixels/2){
                temp.y = (int)mEndY -(int)(item_juli*Math.sin(rangle))-(int)(item_r/2)+initViewHeight/2;
                temp.x =  (int) mEndX+(int)(item_juli*Math.cos(rangle))-(int)(item_r/2)+initViewWidth/2;
            }else{
                temp.y = (int)mEndY -(int)(item_juli*Math.sin(rangle))-(int)(item_r/2)+initViewHeight/2;
                temp.x = (int)mEndX -(int)(item_juli*Math.cos(rangle))-(int)(item_r/2)-initViewWidth/2;
            }
            Log.i("flaot", "x=" + temp.x + "y=" + temp.y);
            //窗体的高度和宽度
            temp.width = DensityUtils.dip2px(context, 40);
            temp.height = DensityUtils.dip2px(context, 40);

            //起始点位置
            temp.gravity = Gravity.TOP | Gravity.LEFT;
            temp.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            //设置窗体类型
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                temp.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
                temp.type = WindowManager.LayoutParams.TYPE_PHONE;
            }
            temp.format = PixelFormat.TRANSPARENT;

            //添加进windowManager
            this.windowManager.addView(imageView, temp);
            itemList.add(imageView);
        }
        //实例化所有item对象
        //计算每一个item的x和y坐标
    }
    //隐藏子app列表，如果有的话
    private void hideItemApp() {
        if(itemList==null){
            return;
        }
        //隐藏所有的item且
        for (ImageView imageView : itemList) {
            this.windowManager.removeView(imageView);
            imageView = null;
        }
        itemList.clear();
    }

    @Override
    public void onClick(View view) {

    }

    //设置弹出框按钮的出现和消失特效
    public void setWindowAnimationStyle(int style){
        this.windowAnimationStyle = style;
    }
    //显示窗口
    public void showWindow(int width, int height, int x, int y) {
        if (this.btn != null) {
            this.windowManager.getDefaultDisplay().getMetrics(displayMetrics);
            params = new WindowManager.LayoutParams();
            //窗体的高度和宽度
            params.width = DensityUtils.dip2px(context, width);
            params.height = DensityUtils.dip2px(context, height);
            initViewHeight = DensityUtils.dip2px(context,height);
            initViewWidth = DensityUtils.dip2px(context,width);
            //起始点位置
            params.gravity = Gravity.TOP | Gravity.LEFT;
            params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
//            if(windowAnimationStyle!=-1){
//                params.windowAnimations = windowAnimationStyle;
//            }
            //设置窗体类型
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
                params.type = WindowManager.LayoutParams.TYPE_PHONE;
            }
            //第一次初始化按钮
            if(mEndX<0&&mEndY<0){
                //判断btn的位置是否在合理的范围之内，因为有了子button的后，btn的y轴不能是从0-屏幕高度了，不然会造成图标的重叠
                if(y<DensityUtils.dip2px(context,100)){
                    y = DensityUtils.dip2px(context,100);
                }
                if(y>(displayMetrics.heightPixels-DensityUtils.dip2px(context,100)-DensityUtils.dip2px(context,y))){
                    y = displayMetrics.heightPixels-DensityUtils.dip2px(context,100)-DensityUtils.dip2px(context,y);
                }
                //判断x的初始位置，采取自动贴边
                x = DensityUtils.dip2px(context,x)<(displayMetrics.widthPixels/2)?0:displayMetrics.widthPixels;
                //模拟模拟第一次的点击位置
                mEndX = DensityUtils.dip2px(context,x);
                mEndY= y;
                params.y = y;
                params.x = x;
            }else{
                //回到上次的位置
                params.x =btnX;
                params.y = btnY;

            }

            params.format = PixelFormat.TRANSPARENT;
            this.windowManager.addView(this.btn, params);
            handler.sendEmptyMessage(100);

        }
//        if(this.item!=null)
//        showItemApp();
    }
    public void setAnimator(){
        mShowAnimatorSet = new AnimatorSet();
        Animator[] showAnimator = new Animator[1];
        showAnimator[0] = ObjectAnimator.ofFloat(this.btn, "scaleX",
                new float[] { 0,0.5f });
        mShowAnimatorSet.playTogether(showAnimator);
        mShowAnimatorSet.setDuration(1500l);

        mHideAnimatorSet = new AnimatorSet();
        Animator[] hideAnimator = new Animator[1];
        hideAnimator[0] = ObjectAnimator.ofFloat(this.btn, "alpha",
                new float[] { 1.0F, 0.0F });
        mHideAnimatorSet.playTogether(hideAnimator);
        mHideAnimatorSet.setDuration(1500l);
        Log.i("setAnimator","zzz");
    }

    //隐藏窗口
    public void hideWindow() {
        if (windowManager != null) {
            if (this.btn != null) {
                mHideAnimatorSet.start();
                this.windowManager.removeView(this.btn);
                hideItemApp();
            }
        }
    }

    //设置动画的插值器和
    public void setAnimation(){

    }


    @Override
    public boolean onTouch(View v, MotionEvent e) {
        WindowManager.LayoutParams layoutParams;

        switch (e.getAction()) {

            case MotionEvent.ACTION_MOVE:
//                Log.i("action_move","move");
                layoutParams = (WindowManager.LayoutParams) v.getLayoutParams();
                if (layoutParams != null) {
                    layoutParams.x = (int) (e.getRawX() - mTouchStartX);
                    layoutParams.y = (int) (e.getRawY() -StatusBarUtils.getStatusHeight(context)- mTouchStartY);
                    this.windowManager.updateViewLayout(v, layoutParams);

                }

                break;
            case MotionEvent.ACTION_DOWN:
                Log.i("action_down","down");
                if(btnStatus==2&&!itemIsMoveHide){
                    hideItemApp();
                    btnStatus = 0;
                    itemIsMoveHide = true;
                }
                mLastTime = System.currentTimeMillis();
                //记录下touch的起点
                mTouchStartY = e.getY();
                mTouchStartX = e.getX();
                mStartX = e.getRawX();
                mStartY = e.getRawY();
                //如果有子app的话，隐藏
                break;
            case MotionEvent.ACTION_UP:
                Log.i("action_up","up");
                layoutParams = (WindowManager.LayoutParams) v.getLayoutParams();
                if (layoutParams != null) {
                    mEndX = e.getRawX();
                    mEndY = e.getRawY();
                    btnX = layoutParams.x <=(displayMetrics.widthPixels/2) ?0:displayMetrics.widthPixels;
                    btnY = (int)(e.getRawY()-StatusBarUtils.getStatusHeight(context) -mTouchStartY);
                    //
                    //判断btn的位置是否在合理的范围之内，因为有了子button的后，btn的y轴不能是从0-屏幕高度了，不然会造成图标的重叠
                    if(btnY<DensityUtils.dip2px(context,100)){
                        btnY = DensityUtils.dip2px(context,100);
                    }
                    if(btnY>(displayMetrics.heightPixels-DensityUtils.dip2px(context,100)-initViewHeight)){
                        btnY = displayMetrics.heightPixels-DensityUtils.dip2px(context,100)-initViewHeight;
                    }
                    layoutParams.x = btnX;
                    layoutParams.y = btnY;
                    mCurrentTime = System.currentTimeMillis();
                    this.windowManager.updateViewLayout(v, layoutParams);
                    if(itemIsMoveHide) {
                        itemIsMoveHide = false;
                        return true;
                    }
                    if(mCurrentTime-mLastTime<800){
                        if(Math.abs(mStartX-mEndX)<10.0 && Math.abs(mStartY-mEndY)<10.0){
                            //更新up动作的btn rawY,rawX
                            mEndY = btnY;
                            mEndX = btnX;
                            //如果是沒有設置子button列表
                            if(this.item==null){
                                floatWindowOnClickListener.onBtnClick();
                                return true;
                            }
                            //如果有設置子button的時候展開子列表
                            if(btnStatus==0){
                                showItemApp();
                                btnStatus = 2;
                            }else if(btnStatus==2){
                                hideItemApp();
                                btnStatus = 0;
                            }else if(btnStatus==1){

                            }
//                            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
//                            activityManager.moveTaskToFront(((Activity)context).getTaskId(), ActivityManager.MOVE_TASK_WITH_HOME);
                        }
                    }
                }
//                showItemApp();
                break;
        }
        return true;
    }

    //悬浮按钮的状态管理
    public void btnStatusManage(){

    }

    public interface FloatItemOnClickListener{
        void onItemClick(int position);
    }
    public interface  FloatWindowOnClickListener{
        void onBtnClick();
    }

}
