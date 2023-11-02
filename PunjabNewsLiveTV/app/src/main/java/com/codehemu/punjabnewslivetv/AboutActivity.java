package com.codehemu.punjabnewslivetv;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.util.Objects;

public class AboutActivity extends AppCompatActivity {
    TextView website,email,policy,version;
    Button button;
    private AdView adView;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.about_app);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.activity_about);
        website = findViewById(R.id.webSite);
        email = findViewById(R.id.Email);
        policy = findViewById(R.id.policy);
        version = findViewById(R.id.version);
        button = findViewById(R.id.contain);

        button.setOnClickListener(v -> startActivity(new Intent(AboutActivity.this, ListingActivity.class).
                putExtra("activity","containing")));

        website.setOnClickListener(v -> {
            String url = getString(R.string.my_website);
            openLink(url);
        });
        email.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            String emailID = getString(R.string.my_email);
            String AppNAME = getString(R.string.app_name);
            Uri data = Uri.parse("mailto:"
                    + emailID
                    + "?subject=" +AppNAME+ " Feedback" + "&body=" + "");
            intent.setData(data);
            startActivity(intent);
        });
        String versionName = BuildConfig.VERSION_NAME;
        version.setText("Version "+versionName);

        policy.setOnClickListener(v -> WebViewPage(getString(R.string.policy_url)));

        loadFacebookAds();

    }
    private void WebViewPage(String url) {
        Intent t = new Intent(AboutActivity.this, WebActivity.class);
        t.putExtra("title", "Privacy Policy");
        t.putExtra("url",url);
        startActivity(t);
    }
    public void openLink(String url){
        Intent linkOpen = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(linkOpen);
    }

    public void loadFacebookAds() {
        MobileAds.initialize(this, initializationStatus -> {
        });

        adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

}