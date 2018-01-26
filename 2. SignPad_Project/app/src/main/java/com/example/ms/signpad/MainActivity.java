package com.example.ms.signpad;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

// 업무 내용
// html canvas 클릭 시 안드로이드에서 SignPad 창 띄우기
// SignPad 창에 서명하기(필압 효과 포함)
// Spen SDK 이용

// 설명
// MainActivity: webview 있는 기본 화면, 자바스크립트와 안드로이드 연동
// SignPad: Spen SDK 이용해서 필압 효과
// libs 폴더에 jsoup-1.8.2.jar  pen-v5.0.0_full.aar  sdk-v1.0.0.jar 있음

public class MainActivity extends AppCompatActivity {
    WebView webView;
    WebSettings webSettings;
    String id;

    @SuppressLint("JavascriptInterface")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = (WebView) findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);

        String userAgent = webView.getSettings().getUserAgentString();
        webView.getSettings().setUserAgentString(userAgent+"SignPad");

        webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new AndroidBridge(webView), "SignPad");
        webView.loadUrl("file:///android_asset/index.html");            // Android에서 Javascript함수 호출
        webView.setWebViewClient(new WebViewClientClass());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String base64_image = data.getStringExtra("data");
        if(resultCode == RESULT_OK) {
            if(data.getStringExtra("result").equals("Save")){
                webView.loadUrl("javascript:Save('"+id+"', '"+base64_image+"');");
            }
            if(data.getStringExtra("result").equals("Clear")){
                webView.loadUrl("javascript:Clear('"+id+"');");
            }
        }
    }

    private class AndroidBridge {                                       // 자바스크립트와 안드로이드 연동
        private WebView webView;

        public AndroidBridge(WebView webView) {
            this.webView = webView;
        }

        @JavascriptInterface
        public void callAndroid(final String arg) {
            Log.e("SignPad", arg);
            id = arg;
            Intent intent = new Intent(
                    getApplicationContext(),
                    SignPad.class);
            startActivityForResult(intent, 201);
        }
    }

    private class WebViewClientClass extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }
}
