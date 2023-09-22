package com.thebrokenrail.combustible.activity;

import android.app.DownloadManager;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.bumptech.glide.request.target.DrawableImageViewTarget;
import com.google.android.material.appbar.MaterialToolbar;
import com.ortiz.touchview.TouchImageView;
import com.thebrokenrail.combustible.R;
import com.thebrokenrail.combustible.util.EdgeToEdge;
import com.thebrokenrail.combustible.util.glide.GlideApp;
import com.thebrokenrail.combustible.util.glide.GlideUtil;

import java.util.Objects;

public class ViewImageActivity extends AppCompatActivity {
    public static final String IMAGE_URL_EXTRA = "com.thebrokenrail.combustible.IMAGE_URL_EXTRA";

    private WindowInsetsControllerCompat windowInsetsController;
    private boolean uiVisible = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_view_image);

        // Toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        // Load Image
        String url = getUrl();
        TouchImageView imageView = findViewById(R.id.view_image);
        imageView.setSuperZoomEnabled(false);
        imageView.setMaxZoom(4);
        RequestManager requestManager = GlideApp.with(this);
        GlideUtil.load(this, requestManager, url, new FitCenter(), 0, false, true, new ColorDrawable(Color.TRANSPARENT), new DrawableImageViewTarget(imageView));

        // Hide/Show UI
        windowInsetsController = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        windowInsetsController.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        imageView.setOnClickListener(v -> {
            if (uiVisible) {
                hideUi();
            } else {
                showUi();
            }
            uiVisible = !uiVisible;
        });

        // Edge-To-Edge
        FrameLayout root = findViewById(R.id.view_image_root);
        EdgeToEdge.setupRoot(root);
    }

    private String getUrl() {
        String url = getIntent().getStringExtra(IMAGE_URL_EXTRA);
        if (url == null) {
            throw new RuntimeException();
        }
        return url;
    }

    private void hideUi() {
        Objects.requireNonNull(getSupportActionBar()).hide();
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());
    }
    private void showUi() {
        Objects.requireNonNull(getSupportActionBar()).show();
        windowInsetsController.show(WindowInsetsCompat.Type.systemBars());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.view_image_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        } else if (item.getItemId() == R.id.view_image_share) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, getUrl());
            sendIntent.setType("text/plain");
            Intent shareIntent = Intent.createChooser(sendIntent, null);
            startActivity(shareIntent);
            return true;
        } else if (item.getItemId() == R.id.view_image_download) {
            // Parse URL
            Uri uri = Uri.parse(getUrl());

            // Get Filename
            String filename = uri.getPath();
            assert filename != null;
            int cut = filename.lastIndexOf('/');
            if (cut != -1) {
                filename = filename.substring(cut + 1);
            }

            // Get MIME Type
            String mimeType = MimeTypeMap.getFileExtensionFromUrl(filename);
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(mimeType);
            if (mimeType == null) {
                mimeType = "*/*";
            }

            // Create Request
            DownloadManager.Request request = new DownloadManager.Request(uri)
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI)
                    .setAllowedOverMetered(true)
                    .setAllowedOverRoaming(true)
                    .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename)
                    .setMimeType(mimeType);
            request.allowScanningByMediaScanner();

            // Start Download
            DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            downloadManager.enqueue(request);

            // Return
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}
