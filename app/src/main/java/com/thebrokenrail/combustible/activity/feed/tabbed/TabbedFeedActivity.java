package com.thebrokenrail.combustible.activity.feed.tabbed;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.ActionBar;
import androidx.core.view.WindowCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.thebrokenrail.combustible.R;
import com.thebrokenrail.combustible.activity.LemmyActivity;
import com.thebrokenrail.combustible.activity.feed.FeedAdapter;
import com.thebrokenrail.combustible.activity.feed.prerequisite.FeedPrerequisite;
import com.thebrokenrail.combustible.activity.feed.prerequisite.FeedPrerequisites;
import com.thebrokenrail.combustible.api.method.GetSiteResponse;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Activity with multiple infinitely-scrolling feeds.
 */
public abstract class TabbedFeedActivity extends LemmyActivity {
    private final List<Map.Entry<Integer, FeedAdapter<?>>> tabs = new ArrayList<>();
    protected ViewPager2 viewPager = null;
    private FeedPrerequisites prerequisites;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_tabbed_feed);

        // Setup Toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = Objects.requireNonNull(getSupportActionBar());
        actionBar.setDisplayHomeAsUpEnabled(true);

        // Prepare View Pager
        viewPager = findViewById(R.id.tabbed_feed_view_pager);
        //WindowInsetsApplier.install(viewPager);

        // Create Tabs
        createTabs();

        // Load Site
        prerequisites = new ViewModelProvider(this).get(FeedPrerequisites.class);
        addPrerequisites(prerequisites);
        prerequisites.setup();
        for (Map.Entry<Integer, FeedAdapter<?>> tab : tabs) {
            tab.getValue().setPrerequisites(prerequisites);
        }
        prerequisites.start(connection);

        // Setup View Pager
        viewPager.setAdapter(new TabbedFeedAdapter(prerequisites, tabs));
        AppBarLayout appBarLayout = findViewById(R.id.app_bar_layout);
        ViewPager2.OnPageChangeCallback onPageChangeCallback = new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                viewPager.post(() -> {
                    RecyclerView recyclerView = viewPager.findViewWithTag("tab-" + position);
                    assert recyclerView != null;
                    appBarLayout.setLiftOnScrollTargetView(recyclerView);
                    appBarLayout.setLifted(recyclerView.canScrollVertically(-1) || recyclerView.getScrollY() > 0);
                });
            }
        };
        viewPager.registerOnPageChangeCallback(onPageChangeCallback);
        onPageChangeCallback.onPageSelected(viewPager.getCurrentItem());

        // Setup Tab Layout
        TabLayout tabLayout = findViewById(R.id.tabbed_feed_tabs);
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> tab.setText(tabs.get(position).getKey())).attach();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.tabbed_feed_toolbar, menu);
        menu.findItem(R.id.feed_share).setVisible(canShare());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (item.getItemId() == R.id.feed_refresh) {
            for (Map.Entry<Integer, FeedAdapter<?>> tab : tabs) {
                FeedAdapter<?> adapter = tab.getValue();
                adapter.refresh(true, () -> {});
            }
            return true;
        } else if (item.getItemId() == R.id.feed_share) {
            share();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Add tab adapter to activity.
     * @param name The tab's name
     * @param adapter The tab's adapter
     */
    protected void addTab(@StringRes int name, FeedAdapter<?> adapter) {
        tabs.add(new AbstractMap.SimpleImmutableEntry<>(name, adapter));
    }

    /**
     * Create all tab adapters.
     */
    protected abstract void createTabs();

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
