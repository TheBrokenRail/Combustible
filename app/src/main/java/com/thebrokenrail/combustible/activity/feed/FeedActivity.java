package com.thebrokenrail.combustible.activity.feed;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.thebrokenrail.combustible.R;
import com.thebrokenrail.combustible.activity.feed.util.FeedUtil;
import com.thebrokenrail.combustible.activity.feed.util.prerequisite.FeedPrerequisite;
import com.thebrokenrail.combustible.activity.feed.util.prerequisite.FeedPrerequisites;
import com.thebrokenrail.combustible.api.method.GetSiteResponse;
import com.thebrokenrail.combustible.util.Util;

/**
 * Activity with a single infinitely-scrolling feed.
 */
public abstract class FeedActivity extends HamburgerActivity {
    private FeedPrerequisites prerequisites;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_feed);

        // Setup Toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Setup Adapter
        onCreateBeforeAdapter();
        RecyclerView feed = findViewById(R.id.feed);
        adapter = createAdapter(feed);
        FeedUtil.setupRecyclerView(feed);
        feed.setAdapter(adapter);

        // Edge-To-Edge
        ViewCompat.setOnApplyWindowInsetsListener(feed, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            feed.setPadding(0, 0, 0, insets.bottom + getResources().getDimensionPixelSize(R.dimen.feed_item_margin));
            return windowInsets;
        });
        CoordinatorLayout root = findViewById(R.id.feed_root);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            root.setPadding(insets.left, 0, insets.right, 0);
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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.feed_refresh) {
            adapter.refresh(true, () -> {});
            return true;
        } else if (item.getItemId() == R.id.feed_share) {
            share();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
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
                        Glide.with(FeedActivity.this)
                                .load(Util.getThumbnailUrl(avatar))
                                .circleCrop()
                                .placeholder(viewProfileTarget.placeholder.newDrawable())
                                .into(viewProfileTarget);
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
}