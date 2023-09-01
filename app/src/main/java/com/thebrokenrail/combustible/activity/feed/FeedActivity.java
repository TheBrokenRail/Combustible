package com.thebrokenrail.combustible.activity.feed;

import android.app.SearchManager;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.widget.SearchView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;
import com.google.android.material.badge.ExperimentalBadgeUtils;
import com.thebrokenrail.combustible.R;
import com.thebrokenrail.combustible.activity.feed.util.FeedUtil;
import com.thebrokenrail.combustible.activity.feed.util.prerequisite.FeedPrerequisite;
import com.thebrokenrail.combustible.activity.feed.util.prerequisite.FeedPrerequisites;
import com.thebrokenrail.combustible.api.method.GetSiteResponse;
import com.thebrokenrail.combustible.api.method.GetUnreadCountResponse;
import com.thebrokenrail.combustible.util.EdgeToEdge;
import com.thebrokenrail.combustible.util.Util;
import com.thebrokenrail.combustible.util.glide.GlideApp;
import com.thebrokenrail.combustible.util.glide.GlideUtil;

/**
 * Activity with a single infinitely-scrolling feed.
 */
public abstract class FeedActivity extends HamburgerActivity {
    private FeedAdapter<?> adapter = null;
    private FeedPrerequisites prerequisites;

    private MaterialToolbar toolbar;

    private BadgeDrawable notificationBadge = null;
    private int unreadNotifications = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_feed);

        // Setup Toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Setup Adapter
        onCreateBeforeAdapter();
        RecyclerView feed = findViewById(R.id.feed);
        adapter = createAdapter(feed);
        FeedUtil.setupRecyclerView(feed);
        feed.setAdapter(adapter);

        // Edge-To-Edge
        EdgeToEdge.setupScroll(feed);
        CoordinatorLayout root = findViewById(R.id.feed_root);
        EdgeToEdge.setupRoot(root);

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
        // Inflate Menu
        getMenuInflater().inflate(R.menu.feed_toolbar, menu);
        menu.findItem(R.id.feed_share).setVisible(canShare());
        menu.findItem(R.id.search).setVisible(canSearch());

        // Setup Search
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        assert searchView != null;
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        // Return
        return true;
    }

    private void createNotificationBadge() {
        // Notification Badge
        notificationBadge = BadgeDrawable.create(this);
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true);
        @ColorInt int primaryColor = ContextCompat.getColor(this, typedValue.resourceId);
        notificationBadge.setBackgroundColor(primaryColor);
        getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnPrimary, typedValue, true);
        @ColorInt int onPrimaryColor = ContextCompat.getColor(this, typedValue.resourceId);
        notificationBadge.setBadgeTextColor(onPrimaryColor);
    }

    @OptIn(markerClass = ExperimentalBadgeUtils.class)
    private void updateNotificationBadge() {
        // Badge
        if (notificationBadge != null) {
            // https://stackoverflow.com/a/66386279/16198887
            BadgeUtils.detachBadgeDrawable(notificationBadge, toolbar, R.id.feed_open_menu);
            notificationBadge = null;
        }
        if (unreadNotifications > 0) {
            createNotificationBadge();
            notificationBadge.setNumber(unreadNotifications);
            BadgeUtils.attachBadgeDrawable(notificationBadge, toolbar, R.id.feed_open_menu);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean ret = super.onPrepareOptionsMenu(menu);
        // Badge
        updateNotificationBadge();
        // Return
        return ret;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Fix Bug Where Badge Isn't Recreated When Dark Mode Is Enabled/Disabled
        updateNotificationBadge();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.feed_refresh) {
            adapter.refresh(true, true, () -> {});
            return true;
        } else if (item.getItemId() == R.id.feed_share) {
            share();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

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
                    updateNavigation();
                }

                // Show Legal Information
                String legal = getSiteResponse.site_view.local_site.legal_information;
                infoLegal.set(legal);
                updateNavigation();

                // View Profile Avatar
                if (getSiteResponse.my_user != null) {
                    boolean showAvatars = getSiteResponse.my_user.local_user_view.local_user.show_avatars;
                    String avatar = getSiteResponse.my_user.local_user_view.person.avatar;
                    if (showAvatars && avatar != null) {
                        RequestManager requestManager = GlideApp.with(FeedActivity.this);
                        String thumbnailUrl = Util.getThumbnailUrl(avatar);
                        Drawable placeholder = viewProfileTarget.placeholder.newDrawable();
                        GlideUtil.load(requestManager, thumbnailUrl, new CircleCrop(), 0, false, false, placeholder, viewProfileTarget);
                    }
                }
            } else if (prerequisite == FeedPrerequisites.COMPLETED) {
                // Sanity Check
                assert infoCommunity.isSetup();
                assert infoLegal.isSetup();
                assert !canBlockCommunity || isCommunityBlocked != null;
                assert !connection.hasToken() || currentUser != null;
            }
        });
        // Notification Badge
        if (connection.hasToken()) {
            prerequisites.add(new FeedPrerequisite.UnreadCount());
            prerequisites.listen(prerequisite -> {
                if (prerequisite instanceof FeedPrerequisite.UnreadCount) {
                    GetUnreadCountResponse getUnreadCountResponse = ((FeedPrerequisite.UnreadCount) prerequisite).get();
                    unreadNotifications = getUnreadCountResponse.replies + getUnreadCountResponse.mentions + getUnreadCountResponse.private_messages;
                    invalidateOptionsMenu();
                }
            });
        }
    }

    /**
     * Check if the share button should be visible.
     * @return True if it should be visible, false otherwise
     */
    protected boolean canShare() {
        return false;
    }

    /**
     * Check if the search button should be visible.
     * @return True if it should be visible, false otherwise
     */
    protected boolean canSearch() {
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

    @Override
    protected void handleEdit(Object element) {
        adapter.handleEdit(element);
    }
}