package com.thebrokenrail.combustible.activity.feed;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.thebrokenrail.combustible.R;
import com.thebrokenrail.combustible.activity.LemmyActivity;
import com.thebrokenrail.combustible.activity.SettingsActivity;
import com.thebrokenrail.combustible.activity.fullscreen.LoginActivity;
import com.thebrokenrail.combustible.util.Config;

import okhttp3.HttpUrl;

public abstract class FeedActivity extends LemmyActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final int DRAWER_GRAVITY = GravityCompat.END;
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_feed);

        // Setup Toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Setup Menu
        NavigationView navigationView = findViewById(R.id.feed_menu);
        navigationView.getMenu().findItem(R.id.feed_menu_login).setVisible(!connection.hasToken());
        navigationView.getMenu().findItem(R.id.feed_menu_view_profile).setVisible(connection.hasToken());
        navigationView.getMenu().findItem(R.id.feed_menu_logout).setVisible(connection.hasToken());
        navigationView.getMenu().findItem(R.id.feed_menu_register).setVisible(!connection.hasToken());
        navigationView.setNavigationItemSelectedListener(this);
        drawerLayout = findViewById(R.id.feed_drawer_layout);

        // Setup Adapter
        onCreateBeforeAdapter();
        RecyclerView feed = findViewById(R.id.feed);
        adapter = createAdapter(feed);

        // Edge-To-Edge
        ViewCompat.setOnApplyWindowInsetsListener(feed, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemGestures());
            feed.setPadding(insets.left, 0, insets.right, insets.bottom + getResources().getDimensionPixelSize(R.dimen.feed_item_margin));
            return windowInsets;
        });

        // Swipe-To-Refresh
        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.feed_swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            FeedAdapter<?> adapter = (FeedAdapter<?>) feed.getAdapter();
            assert adapter != null;
            adapter.refresh(false, () -> swipeRefreshLayout.setRefreshing(false));
        });
    }

    /**
     * Sub-classes can execute code before {@link #createAdapter(RecyclerView)} by overriding this method
     */
    protected void onCreateBeforeAdapter() {}

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.feed_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.feed_open_menu) {
            drawerLayout.openDrawer(DRAWER_GRAVITY);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.feed_menu_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (item.getItemId() == R.id.feed_menu_login) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            return true;
        } else if (item.getItemId() == R.id.feed_menu_register) {
            Config config = new Config(this);
            HttpUrl url = config.getInstance();
            url = url.newBuilder().addPathSegments("signup").build();
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url.toString()));
            startActivity(intent);
            return true;
        } else if (item.getItemId() == R.id.feed_menu_logout) {
            Config config = new Config(this);
            config.setToken(null);
            recreate();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(DRAWER_GRAVITY)) {
            // Close Drawer
            drawerLayout.closeDrawer(DRAWER_GRAVITY);
        } else {
            super.onBackPressed();
        }
    }

    private FeedAdapter<?> adapter = null;
    protected abstract FeedAdapter<?> createAdapter(RecyclerView feed);
    protected FeedAdapter<?> getAdapter() {
        return adapter;
    }
}