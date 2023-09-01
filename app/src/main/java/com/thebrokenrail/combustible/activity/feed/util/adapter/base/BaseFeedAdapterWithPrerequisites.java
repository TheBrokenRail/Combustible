package com.thebrokenrail.combustible.activity.feed.util.adapter.base;

import android.view.View;

import androidx.lifecycle.ViewModelProvider;

import com.thebrokenrail.combustible.activity.feed.util.prerequisite.FeedPrerequisites;
import com.thebrokenrail.combustible.api.Connection;
import com.thebrokenrail.combustible.util.Util;

/**
 * {@link BaseFeedAdapter} with support for {@link FeedPrerequisites}.
 * @param <T> Type of element in list
 */
abstract class BaseFeedAdapterWithPrerequisites<T> extends BaseFeedAdapter<T> {
    private FeedPrerequisites prerequisites = null;

    public BaseFeedAdapterWithPrerequisites(View parent, Connection connection, ViewModelProvider viewModelProvider) {
        super(parent, connection, viewModelProvider);

        // Check
        parent.post(() -> {
            if (prerequisites == null) {
                throw new RuntimeException();
            }
        });
    }

    /**
     * Handle prerequisites. Override this to attach prerequisite listeners.
     * @param prerequisites The new prerequisites
     */
    protected void handlePrerequisites(FeedPrerequisites prerequisites) {
    }

    /**
     * Add prerequisites.
     * @param prerequisites The new prerequisites
     */
    public void setPrerequisites(FeedPrerequisites prerequisites) {
        handlePrerequisites(prerequisites);

        // Check Status
        this.prerequisites = prerequisites;
        if (viewModel.dataset.size() > 0 && !arePrerequisitesLoaded()) {
            throw new RuntimeException();
        }
        if (prerequisites.isError()) {
            updateLoadingStatus(LoadingStatus.ERROR);
        }

        // Check If Everything Is Loaded
        prerequisites.listen(prerequisite -> {
            if (prerequisite == FeedPrerequisites.ERROR) {
                // Error
                if (arePrerequisitesLoaded()) {
                    // Don't Change State For Refresh Errors
                    Util.unknownError(parent.getContext());
                } else {
                    // Update Feed
                    assert prerequisites.isError();
                    updateLoadingStatus(LoadingStatus.ERROR);
                }
            } else if (prerequisite == FeedPrerequisites.COMPLETED) {
                // All Prerequisites Loaded
                startFirstPageLoadIfNeeded();
            } else if (prerequisite == FeedPrerequisites.RETRY_STARTED) {
                // Retry Started
                updateLoadingStatus(LoadingStatus.PENDING);
            }
        });
    }

    @Override
    protected boolean arePrerequisitesLoaded() {
        return prerequisites != null && prerequisites.areLoaded();
    }

    private boolean checkPrerequisites() {
        // Check If Prerequisites Are Loaded
        if (!arePrerequisitesLoaded()) {
            if (prerequisites.isError()) {
                // Retry Failed Prerequisites
                prerequisites.retry(connection);
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    void load() {
        // Check If Prerequisites Are Loaded
        if (checkPrerequisites()) {
            return;
        }

        // Load
        super.load();
    }

    @Override
    public void refresh(boolean hard, boolean refreshPrerequisites, Runnable callback) {
        // Check If Prerequisites Are Loaded
        if (checkPrerequisites()) {
            callback.run();
            return;
        }

        // Refresh Prerequisites
        if (refreshPrerequisites) {
            prerequisites.refresh(connection);
        }

        // Refresh
        super.refresh(hard, refreshPrerequisites, callback);
    }
}
