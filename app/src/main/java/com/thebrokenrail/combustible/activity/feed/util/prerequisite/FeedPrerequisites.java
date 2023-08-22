package com.thebrokenrail.combustible.activity.feed.util.prerequisite;

import androidx.lifecycle.ViewModel;

import com.thebrokenrail.combustible.api.Connection;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class that tracks the prerequisite information to load a feed.
 */
public class FeedPrerequisites extends ViewModel {
    /**
     * Special prerequisite triggered when all other prerequisites are loaded.
     */
    public final static FeedPrerequisite<Object> COMPLETED = new FeedPrerequisite<Object>() {
        @Override
        protected Connection.Method<Object> prepare() {
            throw new RuntimeException();
        }
    };

    /**
     * Callback for listeners.
     */
    public interface Listener {
        /**
         * Callback that is executed on prerequisite event.
         * @param prerequisite The loaded prerequisite (or null if there was an error)
         */
        void onEvent(FeedPrerequisite<?> prerequisite);
    }

    private final List<Listener> listeners = new ArrayList<>();

    /**
     * Attach listener.
     * @param listener The new listener
     */
    public void listen(Listener listener) {
        listeners.add(listener);
    }

    /**
     * Remove all listeners.
     */
    public void clearListeners() {
        listeners.clear();
    }

    private void onEvent(FeedPrerequisite<?> prerequisite) {
        for (Listener listener : listeners) {
            listener.onEvent(prerequisite);
        }
    }

    private final List<FeedPrerequisite<?>> pendingPrerequisites = new ArrayList<>();
    private final List<FeedPrerequisite<?>> failedPrerequisites = new ArrayList<>();
    private final List<FeedPrerequisite<?>> successfulPrerequisites = new ArrayList<>();
    private final List<FeedPrerequisite<?>> refreshingPrerequisites = new ArrayList<>();

    private <T> void load(Connection connection, FeedPrerequisite<T> prerequisite) {
        connection.send(prerequisite.prepare(), t -> {
            prerequisite.value = t;
            boolean refreshing = refreshingPrerequisites.contains(prerequisite);
            if (refreshing) {
                assert successfulPrerequisites.contains(prerequisite);
                refreshingPrerequisites.remove(prerequisite);
            } else {
                assert pendingPrerequisites.contains(prerequisite);
                pendingPrerequisites.remove(prerequisite);
                successfulPrerequisites.add(prerequisite);
            }
            onEvent(prerequisite);
            if (!refreshing && areLoaded()) {
                onEvent(COMPLETED);
            }
        }, () -> {
            boolean refreshing = refreshingPrerequisites.contains(prerequisite);
            if (refreshing) {
                // Ignore Errors When Prerequisite Has Previously Loaded
                assert successfulPrerequisites.contains(prerequisite);
                refreshingPrerequisites.remove(prerequisite);
            } else {
                prerequisite.value = null;
                failedPrerequisites.add(prerequisite);
            }
            onEvent(null);
        });
    }

    /**
     * Add required prerequisite.
     * @param prerequisite The new prerequisite
     */
    public void add(FeedPrerequisite<?> prerequisite) {
        if (!isSetup) {
            pendingPrerequisites.add(prerequisite);
        }
    }

    /**
     * Retry failed prerequisites.
     * @param connection The connection to Lemmy
     */
    public void retry(Connection connection) {
        for (FeedPrerequisite<?> prerequisite : failedPrerequisites) {
            load(connection, prerequisite);
        }
        failedPrerequisites.clear();
    }

    /**
     * Check if all prerequisites are loaded.
     * @return True if all prerequisites are loaded, false otherwise
     */
    public boolean areLoaded() {
        return pendingPrerequisites.size() == 0;
    }

    /**
     * Check if the specified type of prerequisite is included.
     * @param klass The type of prerequisite to check for
     */
    public void require(Class<? extends FeedPrerequisite<?>> klass) {
        List<FeedPrerequisite<?>> allPrerequisites = new ArrayList<>();
        allPrerequisites.addAll(pendingPrerequisites);
        allPrerequisites.addAll(successfulPrerequisites);
        for (FeedPrerequisite<?> prerequisite : allPrerequisites) {
            if (prerequisite.getClass().equals(klass)) {
                return;
            }
        }
        throw new RuntimeException();
    }

    /**
     * Start loading prerequisites.
     * @param connection The connection to Lemmy
     */
    public void start(Connection connection) {
        // Emit EVents For Already Loaded Prerequisites
        for (FeedPrerequisite<?> prerequisite : successfulPrerequisites) {
            onEvent(prerequisite);
        }
        // Check If All Prerequisites Are Loaded
        if (areLoaded()) {
            // Prerequisites Are Already Loaded, Send Completion Event
            onEvent(COMPLETED);
        } else {
            // Restart Previous Prerequisites
            for (FeedPrerequisite<?> prerequisite : pendingPrerequisites) {
                if (!failedPrerequisites.contains(prerequisite)) {
                    load(connection, prerequisite);
                }
            }
        }
        // Clear Refreshing Prerequisites
        refreshingPrerequisites.clear();
    }

    private boolean isSetup = false;

    /**
     * Mark as setup.
     */
    public void setup() {
        isSetup = true;
    }

    /**
     * Check if an error has occurred.
     * @return True if an error has occurred, false otherwise
     */
    public boolean isError() {
        return failedPrerequisites.size() > 0;
    }

    /**
     * Refresh prerequisite.
     * @param connection The connection to Lemmy
     * @param klass The type of prerequisite to refresh
     */
    public void refresh(Connection connection, Class<? extends FeedPrerequisite<?>> klass) {
        // Can't Refresh Anything If Loading Hasn't Finished
        if (!areLoaded()) {
            throw new RuntimeException();
        }

        // Check If Prerequisite Is Already Refreshing
        for (FeedPrerequisite<?> prerequisite : refreshingPrerequisites) {
            if (prerequisite.getClass() == klass) {
                // Already Refreshing
                return;
            }
        }

        // Refresh
        for (FeedPrerequisite<?> prerequisite : successfulPrerequisites) {
            if (prerequisite.getClass() == klass) {
                // Found
                refreshingPrerequisites.add(prerequisite);
                load(connection, prerequisite);
                return;
            }
        }

        // Couldn't Find Prerequisite
        throw new RuntimeException();
    }
}
