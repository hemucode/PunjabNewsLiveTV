package com.codehemu.punjabnewslivetv;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.codehemu.punjabnewslivetv.models.Channel;
import com.codehemu.punjabnewslivetv.models.Common;
import com.codehemu.punjabnewslivetv.services.YoutubeDataService;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;


public class StreamActivity extends AppCompatActivity {
    PlayerView playerView;
    ImageView fbLink, youtubeLink, webLink, fullScreen;
    TextView Description;
    boolean isFullScreen = false;
    boolean oneTimesAdsLoad = true;

    boolean menuCondition = true;

    ExoPlayer player;
    ProgressBar progressBar,progressBar2;
    private AdView adView1,adView2;
    LinearLayout linearLayout;
    NetworkChangeListener networkChangeListener = new NetworkChangeListener();
    WebView web,web1;
    YoutubeDataService service;

    String title,youtubeID,channelYoutube;
    Button back,Go_live;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stream);

        Channel channel = (Channel) getIntent().getSerializableExtra("channel");
        assert channel != null;
        Objects.requireNonNull(getSupportActionBar()).setTitle(channel.getName());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        title = channel.getName();
        youtubeID = channel.getYoutube();


        playerView = findViewById(R.id.playerView);
        fullScreen = playerView.findViewById(R.id.exo_fullscreen_icon);
        progressBar = findViewById(R.id.progressBar);
        progressBar2 = findViewById(R.id.progressBar2);
        web =  findViewById(R.id.webView);
        web1 =  findViewById(R.id.webView1);

        String category = channel.getCategory();
        if (category.equals("m3u8")) {
            web.setVisibility(View.GONE);
            progressBar2.setVisibility(View.GONE);
            channelYoutube = channel.getYoutube();
            playChannel(channel.getLive_url());
            fullScreen.setOnClickListener(v -> setFullScreen("exo"));
        }else {
            menuCondition = false;
            playerView.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
            playChannel("");

            if (category.equals("api")) {
                service = new YoutubeDataService(this);
                String apiUrl = "https://www.googleapis.com/youtube/v3/search?part=snippet&channelId=" + channel.getYoutube() + "&eventType=live&maxResults=1&order=date&type=video&key=" + channel.getLive_url();

                service.getYoutubeData(apiUrl, new YoutubeDataService.OnDataResponse() {
                    @Override
                    public Void onError(String error) {
                        web.loadUrl(" https://www.youtube.com/embed/live_stream?channel=" + channel.getYoutube());
                        return null;
                    }

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("items");
                            JSONObject jsonObject = jsonArray.getJSONObject(0);
                            if (!jsonObject.getString("id").isEmpty()) {
                                JSONObject jsonObject1 = new JSONObject(jsonObject.getString("id"));
                                web.loadUrl("https://www.youtube.com/embed/" + jsonObject1.getString("videoId"));
                            } else {
                                web.loadUrl(" https://www.youtube.com/embed/live_stream?channel=" + channel.getYoutube());
                            }

                        } catch (JSONException e) {
                            web.loadUrl(" https://www.youtube.com/embed/live_stream?channel=" + channel.getYoutube());
                        }

                    }
                });


            } else if (category.equals("yt")) {
                web.loadUrl(channel.getLive_url() + channel.getYoutube());
            }

            web.getSettings().setJavaScriptEnabled(true);
            web.getSettings().setLoadWithOverviewMode(true);
            web.getSettings().setUseWideViewPort(true);

            web.setWebViewClient(new WebViewClient() {
                @Override
                public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                    super.onReceivedError(view, request, error);
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    String Script = "var css = document.createElement('style');" +
                            "var head = document.head;" +
                            "css.innerText = `" +
                            ".ytp-show-cards-title," +
                            ".ytp-pause-overlay," +
                            ".branding-img," +
                            ".ytp-large-play-button," +
                            ".ytp-youtube-button," +
                            ".ytp-menuitem:nth-child(1)," +
                            ".ytp-small-redirect," +
                            ".ytp-menuitem:nth-child(4)" +
                            "{display:none !important;}`;" +
                            "head.appendChild(css);" +
                            "document.querySelector('.ytp-play-button').click();" +
                            "css.type = 'text/css';" +
                            "if(document.querySelector('.ytp-error-content-wrap-reason')){Android.showToast(`error`);}else{Android.showToast(`noError`);}" +
                            "let ytpFullscreenButton = document.querySelector('.ytp-fullscreen-button');" +
                            "ytpFullscreenButton.addEventListener('click', function() { Android.showToast(`toast`); });";

                    web.evaluateJavascript(Script, null);
                    try {
                        Thread.sleep(1000);
                        progressBar2.setVisibility(View.GONE);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });

            web.addJavascriptInterface(new WebAppInterface(this), "Android");
        }


        fbLink = findViewById(R.id.fbLink);

        fbLink.setOnClickListener(v -> openLink("https://www.facebook.com/"+channel.getFacebook()));

        youtubeLink = findViewById(R.id.youtubeLink);
        youtubeLink.setOnClickListener(v -> openLink("https://www.youtube.com/channel/"+ channel.getYoutube()));

        webLink = findViewById(R.id.webLink);
        webLink.setOnClickListener(v -> openLink(channel.getWebsite()));

        Description = findViewById(R.id.channelDes);
        Description.setText(channel.getDescription());
        Description.setSelected(true);

    }

    @SuppressLint("SetJavaScriptEnabled")
    private void stepChange(String channelYoutube) {
        web1.loadUrl(" https://www.youtube.com/embed/live_stream?channel="+ channelYoutube);
        web1.getSettings().setJavaScriptEnabled(true);
        web1.getSettings().setLoadWithOverviewMode(true);
        web1.getSettings().setUseWideViewPort(true);

        web1.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                String Script = "var css = document.createElement('style');" +
                        "var head = document.head;" +
                        "css.innerText = `" +
                        ".ytp-show-cards-title," +
                        ".ytp-pause-overlay," +
                        ".branding-img," +
                        ".ytp-large-play-button," +
                        ".ytp-youtube-button," +
                        ".ytp-menuitem:nth-child(1)," +
                        ".ytp-small-redirect," +
                        ".ytp-menuitem:nth-child(4)" +
                        "{display:none !important;}`;" +
                        "head.appendChild(css);" +
                        "document.querySelector('.ytp-play-button').click();" +
                        "css.type = 'text/css';"+
                        "if(document.querySelector('.ytp-error-content-wrap-reason')){Android.showToast(`error`);}else{Android.showToast(`noErrorWeb1`);}"+
                        "let ytpFullscreenButton = document.querySelector('.ytp-fullscreen-button');" +
                        "ytpFullscreenButton.addEventListener('click', function() { Android.showToast(`toast1`); });";

                web1.evaluateJavascript(Script,null);
            }
        });

        web1.addJavascriptInterface(new WebAppInterface(this), "Android");

    }

    @SuppressLint("SourceLockedOrientationActivity")
    public void setFullScreen(String button){
        if (isFullScreen){
            if(adView1!=null){
                adView1.setVisibility(View.VISIBLE);
            }
            if(adView2!=null){
                adView2.setVisibility(View.VISIBLE);
            }
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
            if (getSupportActionBar() != null){
                getSupportActionBar().show();
            }
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            if(button.equals("yt")){
                ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) web.getLayoutParams();
                params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                params.height = (int) (200 * getApplicationContext().getResources().getDisplayMetrics().density);
                web.setLayoutParams(params);
            }else if(button.equals("yt1")){
                ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) web1.getLayoutParams();
                params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                params.height = (int) (200 * getApplicationContext().getResources().getDisplayMetrics().density);
                web1.setLayoutParams(params);
            }else {
                   ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) playerView.getLayoutParams();
                   params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                   params.height = (int) (200 * getApplicationContext().getResources().getDisplayMetrics().density);
                   playerView.setLayoutParams(params);
            }
            isFullScreen = false;
        }else {
            if(adView1!=null){
                adView1.setVisibility(View.GONE);
            }
            if(adView2!=null){
                adView2.setVisibility(View.GONE);
            }
            if(button.equals("yt")){
                ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) web.getLayoutParams();
                params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                params.height = ViewGroup.LayoutParams.MATCH_PARENT;
                web.setLayoutParams(params);
            } else if(button.equals("yt1")){
                ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) web1.getLayoutParams();
                params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                params.height = ViewGroup.LayoutParams.MATCH_PARENT;
                web1.setLayoutParams(params);
            }else {
                ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) playerView.getLayoutParams();
                params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                params.height = ViewGroup.LayoutParams.MATCH_PARENT;
                playerView.setLayoutParams(params);
            }

            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                    |View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    |View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

            if (getSupportActionBar() != null){
                getSupportActionBar().hide();
            }
            isFullScreen = true;

        }
    }

    public class WebAppInterface {
        Context mContext;
        /** Instantiate the interface and set the context */
        WebAppInterface(Context c) {
            mContext = c;
        }

        @JavascriptInterface
        public void showToast(String toast) {

            if (toast.equals("toast")){
                StreamActivity.this.runOnUiThread(() -> setFullScreen("yt"));
            }
            if (toast.equals("toast1")){
                StreamActivity.this.runOnUiThread(() -> setFullScreen("yt1"));
            }

            if (toast.equals("noError")){
                StreamActivity.this.runOnUiThread(() -> web.setVisibility(View.VISIBLE));
            }

            if (toast.equals("noErrorWeb1")){
                StreamActivity.this.runOnUiThread(() -> {
                    if (player != null) {
                        player.setPlayWhenReady(false);
                        player.stop();
                        player.seekTo(0);
                    }
                    if(adView1!=null){
                        adView1.destroy();
                    }
                    if(adView2!=null){
                        adView2.destroy();
                    }
                    web1.setVisibility(View.VISIBLE);
                    playerView.setVisibility(View.GONE);
                    web.setVisibility(View.GONE);
                    progressBar2.setVisibility(View.GONE);
                    progressBar.setVisibility(View.GONE);
                });
            }


            if (toast.equals("error")){
                StreamActivity.this.runOnUiThread(() -> {

                    final Dialog dialog = new Dialog(StreamActivity.this); // Context, this, etc.
                    dialog.setContentView(R.layout.go_yt_live);
                    back = dialog.findViewById(R.id.back);
                    back.setOnClickListener(v -> dialog.cancel());
                    Go_live = dialog.findViewById(R.id.go_live);
                    Go_live.setOnClickListener(v -> startActivity(new Intent(StreamActivity.this, WebActivity.class).putExtra("title", title).putExtra("url","https://www.youtube.com/channel/"+youtubeID+"/live")));

                    dialog.show();
                });
            }

        }
    }


    public void openLink(String url){
        Intent linkOpen = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(linkOpen);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        if (menuCondition){
            getMenuInflater().inflate(R.menu.yt_live,menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            onBackPressed();
        }

        if (item.getItemId() == R.id.ytLive) {
            if (!channelYoutube.isEmpty()){
                stepChange(channelYoutube);
            }
        }

        return super.onOptionsItemSelected(item);
    }


    public void playChannel(String LiveUrl){
        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);

        DataSource.Factory dataSourceFactory = new DefaultHttpDataSource.Factory();

        HlsMediaSource mediaSource = new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(LiveUrl));

        player.setMediaSource(mediaSource);

        player.prepare();
        player.setPlayWhenReady(true);


        player.addListener(new Player.Listener() {

            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == player.STATE_READY){
                    progressBar.setVisibility(View.GONE);
                    player.setPlayWhenReady(true);
                    if (oneTimesAdsLoad){
                        loadAds();
                        oneTimesAdsLoad = false;
                    }
                }else if (playbackState == player.STATE_BUFFERING){
                    progressBar.setVisibility(View.VISIBLE);
                } else {
                    progressBar.setVisibility(View.GONE);
                    player.setPlayWhenReady(true);

                }
            }


        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        player.seekToDefaultPosition();
        player.setPlayWhenReady(true);
    }

    @Override
    protected void onPause() {
        player.setPlayWhenReady(false);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        player.release();
        super.onDestroy();
    }

    public void loadAds() {
        MobileAds.initialize(this, initializationStatus -> {
        });

        adView1 = findViewById(R.id.adView1);
        adView2 = findViewById(R.id.adView2);
        AdRequest adRequest = new AdRequest.Builder().build();

        adView1.loadAd(adRequest);
        adView2.loadAd(adRequest);

    }
    public class NetworkChangeListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!Common.isConnectToInternet(context)) {
                final Dialog dialog = new Dialog(context); // Context, this, etc.
                dialog.setContentView(R.layout.activity_network);
                linearLayout = dialog.findViewById(R.id.dismiss);
                linearLayout.setOnClickListener(v -> dialog.cancel());
                dialog.show();
            }
        }
    }

    @Override
    protected void onStart() {
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeListener, filter);
        super.onStart();
    }

    @Override
    protected void onStop() {
        unregisterReceiver(networkChangeListener);
        super.onStop();
    }

}