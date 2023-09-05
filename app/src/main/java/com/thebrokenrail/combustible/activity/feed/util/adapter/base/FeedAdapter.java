package com.thebrokenrail.combustible.activity.feed.util.adapter.base;

import android.view.View;

import androidx.lifecycle.ViewModelProvider;

import com.thebrokenrail.combustible.activity.feed.util.prerequisite.FeedPrerequisite;
import com.thebrokenrail.combustible.activity.feed.util.prerequisite.FeedPrerequisites;
import com.thebrokenrail.combustible.api.Connection;
import com.thebrokenrail.combustible.api.method.GetSiteResponse;
import com.thebrokenrail.combustible.util.Permissions;

/**
 * {@link BaseFeedAdapterWithPrerequisites} with general Lemmy instance information.
 * @param <T> Type of element in list
 */
public abstract class FeedAdapter<T> extends BaseFeedAdapterWithPrerequisites<T> {
    /**
     * General instance information.
     */
    protected GetSiteResponse site = null;

    /**
     * Permission manager.
     */
    protected final Permissions permissions = new Permissions();

    public FeedAdapter(View parent, Connection connection, ViewModelProvider viewModelProvider) {
        super(parent, connection, viewModelProvider);
    }

    @Override
    protected void handlePrerequisites(FeedPrerequisites prerequisites) {
        super.handlePrerequisites(prerequisites);
        // General Instance Information
        prerequisites.require(FeedPrerequisite.Site.class);
        prerequisites.listen((prerequisite, isRefreshing) -> {
            if (prerequisite instanceof FeedPrerequisite.Site) {
                site = ((FeedPrerequisite.Site) prerequisite).get();
                // Update Permissions
                permissions.setSite(site);
                // Update Dataset
                notifier.change(0, viewModel.dataset.size());
                // Update Header
                if (hasHeader()) {
                    notifyItemChanged(0);
                }
            }
        });
    }
}
