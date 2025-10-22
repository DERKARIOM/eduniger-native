package com.ninotech.fabi.controleur.activity;

import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.ninotech.fabi.R;
import com.ninotech.fabi.controleur.adapter.StatusBarAdapter;

import java.util.Objects;

public class WebActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_web);
        StatusBarAdapter statusBarAdapter = new StatusBarAdapter(this, getWindow());
// Activer le bouton de retour de l'action barre
        Objects.requireNonNull(getSupportActionBar()).hide();
        progressBar = findViewById(R.id.progress_bar_activity_find_my_phone);
        webView = findViewById(R.id.web_view_activity_find_my_phone);
        mOkRelativeLayout = findViewById(R.id.relative_layout_activity_web_ok);
        mErrorRelativeLayout = findViewById(R.id.relative_layout_activity_web_error);
        mButton = findViewById(R.id.button_activity_web);
        mReloadProgressBar = findViewById(R.id.progress_bar_activity_web_reload);
        String url = "https://clammy-bromine-628.notion.site/27b59184564580c79331e4bd0a07649f?v=27b59184564580ed9a69000cbc5af15b&source=copy_link";
        webView.getSettings().setJavaScriptEnabled(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                mOkRelativeLayout.setVisibility(View.VISIBLE);
                mErrorRelativeLayout.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                mButton.setVisibility(View.VISIBLE);
                mReloadProgressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    mOkRelativeLayout.setVisibility(View.GONE);
                    mErrorRelativeLayout.setVisibility(View.VISIBLE);
                    mButton.setVisibility(View.VISIBLE);
                    mReloadProgressBar.setVisibility(View.INVISIBLE);
                    if (error.getErrorCode() == 404) {
                        Toast.makeText(WebActivity.this, "404", Toast.LENGTH_SHORT).show();
                    } else {
                        super.onReceivedError(view, request, error);
                    }
                }
            }
            @Override
            public void onPageFinished(WebView view, String url) {
                progressBar.setVisibility(View.GONE);
            }

        });

        webView.setWebChromeClient(new WebChromeClient());
        assert url != null;
        webView.loadUrl(url);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mButton.setVisibility(View.INVISIBLE);
                mReloadProgressBar.setVisibility(View.VISIBLE);
                webView.reload();
            }
        });
    }
    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
    private ProgressBar progressBar;
    private ProgressBar mReloadProgressBar;
    private WebView webView;
    private RelativeLayout mOkRelativeLayout;
    private RelativeLayout mErrorRelativeLayout;
    private Button mButton;
}

