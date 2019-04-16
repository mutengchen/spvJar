package com.example.mt.spvjar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;

public class SsoLoginActivity extends Activity {
    private WebView webView;
    private ImageView backImg;
    private SsoListerner ssoListerner;
    public static final int LOGIN_SUCCESS = 1;
    public static final int LOGIN_FAILED = 2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sso_login_layout);
        webView = findViewById(R.id.webview);
        backImg = findViewById(R.id.web_back);
        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        initWebView();
        if(getIntent()!=null){
            String url =  getIntent().getExtras().getString("login_page_url");
            if(url!=null)
                webView.loadUrl(url);
        }
    }

    //设置登录监听回调
    public void setSsoListerner(SsoListerner ssoListerner){
        this.ssoListerner =ssoListerner;
    }

    public void initWebView(){
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setDefaultTextEncodingName("UTF-8");
        webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        // 图片过大时自动适应屏幕
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
        // 禁用水平垂直滚动条
        webView.setHorizontalScrollBarEnabled(false);
        webView.setVerticalScrollBarEnabled(false);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAppCacheEnabled(true);
        webSettings.setSavePassword(false);
        webSettings.setSaveFormData(false);
        webSettings.setSupportMultipleWindows(true);
        webSettings.setPluginState(WebSettings.PluginState.ON_DEMAND);
        //防止系统字体字体大小，对html页面布局的干扰，无论系统字体怎么变化，html显示的字号都不会发生改变
        webSettings.setTextZoom(100);
        // 设置加载进来的页面自适应手机屏幕
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        // (禁止)显示放大缩小Controller
        webSettings.setBuiltInZoomControls(false);
        // (禁止)|(可)缩放
        webSettings.setSupportZoom(false);
        // 不显示webView缩放按钮
        webSettings.setDisplayZoomControls(false);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        // 调整Cookie的使用，否则Cookie的相关操作只能影响系统内核

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webView.setWebContentsDebuggingEnabled(true);
        }
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView webView, String s) {
                if(s.startsWith("https:")||s.startsWith("http:"))
                    webView.loadUrl(s);
                return false;
            }
        });
        webView.addJavascriptInterface(new SsoCallBack() {
            @JavascriptInterface
            @Override
            public void loginSuccess(String token) {
                if(ssoListerner!=null)
                ssoListerner.loginSuccess(token);
//                Intent intent = new Intent();
////                Bundle bundle = new Bundle();
////                bundle.putString("sso_token",token);
////                intent.putExtras(bundle);
////                setResult(LOGIN_SUCCESS,intent);
                finish();
            }

            @JavascriptInterface
            @Override
            public void loginFailed(String err_msg) {
                if(ssoListerner!=null)
                ssoListerner.loginFailed(err_msg);
            }

        },"sso");

    }
    interface SsoCallBack{
        void loginSuccess(String token);
        void loginFailed(String err_msg);
    }
    interface SsoListerner{
        void loginSuccess(String token);
        void loginFailed(String err_msg);
    }
}
