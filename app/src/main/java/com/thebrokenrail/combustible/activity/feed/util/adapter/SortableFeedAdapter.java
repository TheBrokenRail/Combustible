package com.thebrokenrail.combustible.activity.feed.util.adapter;

import android.view.View;

import androidx.lifecycle.ViewModelProvider;

import com.thebrokenrail.combustible.R;
import com.thebrokenrail.combustible.activity.feed.util.adapter.base.FeedAdapter;
import com.thebrokenrail.combustible.activity.feed.util.prerequisite.FeedPrerequisite;
import com.thebrokenrail.combustible.activity.feed.util.prerequisite.FeedPrerequisites;
import com.thebrokenrail.combustible.api.Connection;
import com.thebrokenrail.combustible.widget.Sorter;

/**
 * {@link FeedAdapter} with support for sorting.
 * @param <T> Type of element in list
 */
public abstract class SortableFeedAdapter<T> extends FeedAdapter<T> {
    /**
     * The current sorting configuration.
     */
    protected final Sorter.ViewModel sorting;

    public SortableFeedAdapter(View parent, Connection connection, ViewModelProvider viewModelProvider) {
        super(parent, connection, viewModelProvider);
        sorting = rootViewModel.get(Sorter.ViewModel.class);
    }

    @Override
    protected boolean hasHeader() {
        return true;
    }

    @Override
    protected void bindHeader(View root) {
        // Bind Sorter
        Sorter sorter = root.findViewById(R.id.feed_sorter);
        sorter.bind(sorting, connection.hasToken(), () -> refresh(true, false, () -> {}), this::isSortingTypeVisible);
    }

    /**
     * Check if a sorting category is visible.
     * @param type The sorting category to check
     * @return True if it should be visible, false otherwise
     */
    protected abstract boolean isSortingTypeVisible(Class<? extends Enum<?>> type);

    @Override
    protected void handlePrerequisites(FeedPrerequisites prerequisites) {
        super.handlePrerequisites(prerequisites);
        if (hasHeader()) {
            prerequisites.listen((prerequisite, isRefreshing) -> {
                if (prerequisite == FeedPrerequisites.COMPLETED) {
                    // Mark As Setup
                    sorting.setup();
                    // Reload Header
                    reloadHeader();
                } else if (prerequisite instanceof FeedPrerequisite.Site) {
                    // Site Loaded
                    assert site != null;
                    if (useDefaultSort() && !isRefreshing) {
                        // Custom Sorting Defaults
                        if (site.my_user != null) {
                            sorting.set(site.my_user.local_user_view.local_user.default_sort_type);
                            sorting.set(site.my_user.local_user_view.local_user.default_listing_type);
                        } else {
                            sorting.set(site.site_view.local_site.default_post_listing_type);
                        }
                        reloadHeader();
                    }
                }
            });
        }
    }

    /**
     * Check whether the default sorting configuration from the user/instance should be used.
     * @return True if it should be used, false otherwise
     */
    protected boolean useDefaultSort() {
        return false;
    }
}
