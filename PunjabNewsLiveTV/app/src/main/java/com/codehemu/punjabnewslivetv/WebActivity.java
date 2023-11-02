package com.codehemu.punjabnewslivetv;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.codehemu.punjabnewslivetv.models.Common;
import com.monstertechno.adblocker.AdBlockerWebView;
import com.monstertechno.adblocker.util.AdBlocker;

import java.util.Objects;

public class WebActivity extends AppCompatActivity {
    WebView web;
    ProgressBar progressBar;
    SwipeRefreshLayout mSwipeRefreshLayout;
    LinearLayout linearLayout;
    Button button1,button2;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);
        web = findViewById(R.id.webView);
        progressBar = findViewById(R.id.progressBar3);
        linearLayout = findViewById(R.id.sorryLayout);

        button1 = findViewById(R.id.button3);

        button2 = findViewById(R.id.button4);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            progressBar.setVisibility(View.VISIBLE);
            String title = extras.getString("title");
            String url = extras.getString("url");

            getSupportActionBar().setTitle(title);
            assert url != null;
            web.loadUrl(url);

            button1.setOnClickListener(v -> {
                Intent intent2 = new Intent("android.intent.action.VIEW");
                intent2.setData(Uri.parse(url));
                WebActivity.this.startActivity(intent2);
            });

            button2.setOnClickListener(v -> onBackPressed());
            new AdBlockerWebView.init(this).initializeWebView(web);
            web.getSettings().setJavaScriptEnabled(true);
            web.getSettings().setLoadWithOverviewMode(true);
            web.getSettings().setUseWideViewPort(true);

            web.setWebChromeClient(new MyChrome());
            web.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String request) {

                    return super.shouldOverrideUrlLoading(view, request);
                }
                @SuppressWarnings("deprecation")
                @Override
                public WebResourceResponse shouldInterceptRequest(WebView view, String url) {

                    return AdBlockerWebView.blockAds(view,url) ? AdBlocker.createEmptyResource() :
                            super.shouldInterceptRequest(view, url);

                }
                @Override
                public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                    if (!Common.isConnectToInternet(WebActivity.this)) {
                        web.setVisibility(View.INVISIBLE);
                        linearLayout.setVisibility(View.VISIBLE);
                    }
                    super.onReceivedError(view, request, error);

                }
                @Override
                public void onPageFinished(WebView view, String url) {

                    super.onPageFinished(view, url);
                    progressBar.setVisibility(View.INVISIBLE);
                }

    });

        }


        mSwipeRefreshLayout = findViewById(R.id.refresh_app);

        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            mSwipeRefreshLayout.setRefreshing(false);
            web.loadUrl("javascript:window.location.reload( true )");
        });

    }



    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private class MyChrome extends WebChromeClient {
        private View mCustomView;
        private WebChromeClient.CustomViewCallback mCustomViewCallback;

        private int mOriginalOrientation;
        private int mOriginalSystemUiVisibility;

        MyChrome() {}

        public Bitmap getDefaultVideoPoster()
        {
            if (mCustomView == null) {
                return null;
            }
            return BitmapFactory.decodeResource(getApplicationContext().getResources(), 2130837573);
        }

        public void onHideCustomView()
        {
            ((FrameLayout)getWindow().getDecorView()).removeView(this.mCustomView);
            this.mCustomView = null;
            getWindow().getDecorView().setSystemUiVisibility(this.mOriginalSystemUiVisibility);
            setRequestedOrientation(this.mOriginalOrientation);
            this.mCustomViewCallback.onCustomViewHidden();
            this.mCustomViewCallback = null;
        }

        public void onShowCustomView(View paramView, WebChromeClient.CustomViewCallback paramCustomViewCallback)
        {
            if (this.mCustomView != null)
            {
                onHideCustomView();
                return;
            }
            this.mCustomView = paramView;
            this.mOriginalSystemUiVisibility = getWindow().getDecorView().getSystemUiVisibility();
            this.mOriginalOrientation = getRequestedOrientation();
            this.mCustomViewCallback = paramCustomViewCallback;
            ((FrameLayout)getWindow().getDecorView()).addView(this.mCustomView, new FrameLayout.LayoutParams(-1, -1));
            getWindow().getDecorView().setSystemUiVisibility(3846 | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        web.saveState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        web.restoreState(savedInstanceState);
    }
}
