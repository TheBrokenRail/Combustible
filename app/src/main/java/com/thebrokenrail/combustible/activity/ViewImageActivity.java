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
import android.view.View;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.ortiz.touchview.TouchImageView;
import com.thebrokenrail.combustible.R;
import com.thebrokenrail.combustible.util.DrawableAlwaysCrossFadeFactory;

public class ViewImageActivity extends AppCompatActivity {
    public static final String IMAGE_URL_EXTRA = "com.thebrokenrail.combustible.IMAGE_URL_EXTRA";

    private AppBarLayout appBarLayout;
    private WindowInsetsControllerCompat windowInsetsController;
    private boolean uiVisible = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_view_image);

        // Toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        appBarLayout = (AppBarLayout) toolbar.getParent();
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        // Load Image
        String url = getUrl();
        TouchImageView imageView = findViewById(R.id.view_image);
        imageView.setSuperZoomEnabled(false);
        Glide.with(this)
                .load(url)
                .transition(DrawableTransitionOptions.with(new DrawableAlwaysCrossFadeFactory()))
                .placeholder(new ColorDrawable(Color.TRANSPARENT))
                .fitCenter()
                .into(imageView);

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
    }

    private String getUrl() {
        String url = getIntent().getStringExtra(IMAGE_URL_EXTRA);
        if (url == null) {
            throw new RuntimeException();
        }
        return url;
    }

    private static final long ANIMATION_DURATION = 200;
    private void hideUi() {
        appBarLayout.animate()
                .translationY(-appBarLayout.getHeight())
                .setDuration(ANIMATION_DURATION)
                .withEndAction(() -> appBarLayout.setVisibility(View.GONE));
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());
    }
    private void showUi() {
        appBarLayout.setVisibility(View.VISIBLE);
        appBarLayout.animate()
                .translationY(0)
                .setDuration(ANIMATION_DURATION);
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
            onBackPressed();
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
