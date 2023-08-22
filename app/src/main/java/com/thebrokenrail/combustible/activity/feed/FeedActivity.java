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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.thebrokenrail.combustible.R;
import com.thebrokenrail.combustible.activity.LemmyActivity;
import com.thebrokenrail.combustible.activity.SettingsActivity;
import com.thebrokenrail.combustible.activity.feed.prerequisite.FeedPrerequisite;
import com.thebrokenrail.combustible.activity.feed.prerequisite.FeedPrerequisites;
import com.thebrokenrail.combustible.activity.feed.tabbed.saved.SavedFeedActivity;
import com.thebrokenrail.combustible.activity.feed.tabbed.user.UserFeedActivity;
import com.thebrokenrail.combustible.activity.fullscreen.LoginActivity;
import com.thebrokenrail.combustible.api.method.GetSiteResponse;
import com.thebrokenrail.combustible.util.Config;

import okhttp3.HttpUrl;

/**
 * Activity with a single infinitely-scrolling feed.
 */
public abstract class FeedActivity extends LemmyActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final int DRAWER_GRAVITY = GravityCompat.END;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private FeedPrerequisites prerequisites;

    private int currentUser = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_feed);

        // Setup Toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Setup Menu
        navigationView = findViewById(R.id.feed_menu);
        navigationView.getMenu().findItem(R.id.feed_menu_login).setVisible(!connection.hasToken());
        navigationView.getMenu().findItem(R.id.feed_menu_view_profile).setVisible(connection.hasToken());
        navigationView.getMenu().findItem(R.id.feed_menu_view_profile).setEnabled(false);
        navigationView.getMenu().findItem(R.id.feed_menu_logout).setVisible(connection.hasToken());
        navigationView.getMenu().findItem(R.id.feed_menu_register).setVisible(!connection.hasToken());
        navigationView.getMenu().findItem(R.id.feed_menu_saved).setVisible(connection.hasToken());
        navigationView.setNavigationItemSelectedListener(this);
        drawerLayout = findViewById(R.id.feed_drawer_layout);

        // Setup Adapter
        onCreateBeforeAdapter();
        RecyclerView feed = findViewById(R.id.feed);
        adapter = createAdapter(feed);
        FeedUtil.setupRecyclerView(feed);
        feed.setAdapter(adapter);

        // Edge-To-Edge
        ViewCompat.setOnApplyWindowInsetsListener(feed, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemGestures());
            feed.setPadding(insets.left, 0, insets.right, insets.bottom + getResources().getDimensionPixelSize(R.dimen.feed_item_margin));
            return windowInsets;
        });

        // Swipe-To-Refresh
        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.feed_swipe_refresh_layout);
        FeedUtil.setupSwipeToRefresh(swipeRefreshLayout, adapter);

        // Load Site
        prerequisites = new ViewModelProvider(this).get(FeedPrerequisites.class);
        addPrerequisites(prerequisites);
        prerequisites.setup();
        adapter.setPrerequisites(prerequisites);
        FeedUtil.setupPrerequisites(feed, prerequisites);
        prerequisites.start(connection);
    }

    /**
     * Sub-classes can execute code before {@link #createAdapter(RecyclerView)} by overriding this method
     */
    protected void onCreateBeforeAdapter() {}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.feed_toolbar, menu);
        menu.findItem(R.id.feed_share).setVisible(canShare());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.feed_open_menu) {
            drawerLayout.openDrawer(DRAWER_GRAVITY);
            return true;
        } else if (item.getItemId() == R.id.feed_refresh) {
            adapter.refresh(true, () -> {});
            return true;
        } else if (item.getItemId() == R.id.feed_share) {
            share();
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
            getViewModelStore().clear();
            recreate();
            return true;
        } else if (item.getItemId() == R.id.feed_menu_saved) {
            Intent intent = new Intent(this, SavedFeedActivity.class);
            startActivity(intent);
            return true;
        } else if (item.getItemId() == R.id.feed_menu_view_profile) {
            Intent intent = new Intent(this, UserFeedActivity.class);
            intent.putExtra(UserFeedActivity.USER_ID_EXTRA, currentUser);
            startActivity(intent);
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

    /**
     * Add prerequisites.
     * @param prerequisites Prerequisite tracker object
     */
    protected void addPrerequisites(FeedPrerequisites prerequisites) {
        prerequisites.add(new FeedPrerequisite.Site());
        prerequisites.listen(prerequisite -> {
            if (prerequisite instanceof FeedPrerequisite.Site) {
                GetSiteResponse getSiteResponse = ((FeedPrerequisite.Site) prerequisite).get();

                // TODO Remove Hack
                if (getSiteResponse.my_user != null) {
                    getSiteResponse.my_user.local_user_view.local_user.blur_nsfw = true;
                }

                // Get Current User ID
                if (connection.hasToken() && getSiteResponse.my_user != null) {
                    currentUser = getSiteResponse.my_user.local_user_view.person.id;
                    navigationView.getMenu().findItem(R.id.feed_menu_view_profile).setEnabled(true);
                }
            }
        });
    }

    /**
     * Check if the share button should be visible.
     * @return True if it should be visible, false otherwise
     */
    protected boolean canShare() {
        return false;
    }

    /**
     * Share this page.
     */
    protected void share() {
        throw new RuntimeException();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        prerequisites.clearListeners();
    }
}