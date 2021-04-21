package com.example.findspot;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import static com.example.findspot.ChoiceGPSRandomActivity.address_r;
import static com.example.findspot.ChoiceGPSGroupActivity.position_tmp;

public class DaumWebViewActivity extends AppCompatActivity {

    private WebView browser;

    class MyJavaScriptInterface
    {
        @JavascriptInterface
        @SuppressWarnings("unused")
        public void processDATA(String data) {
            String getExtra_nickName = getIntent().getStringExtra("name");
            if (getExtra_nickName.equals("")) address_r = data;
            else {
                position_tmp.add(new PositionItem(getExtra_nickName, data, 360, 360));
            }

            Intent intent = new Intent();
            setResult(1, intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daum_web_view);

        browser = (WebView) findViewById(R.id.daum_webview);
        browser.getSettings().setJavaScriptEnabled(true);
        browser.addJavascriptInterface(new MyJavaScriptInterface(), "Android");

        browser.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                browser.loadUrl("javascript:sample2_execDaumPostcode();");
            }
        });

        browser.loadUrl("http://222.111.4.158/wheremiddle/daum.html");
    }
}
