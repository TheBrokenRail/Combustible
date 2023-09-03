package com.thebrokenrail.combustible.activity.feed.util.prerequisite;

import androidx.lifecycle.ViewModel;

import com.thebrokenrail.combustible.api.Connection;
import com.thebrokenrail.combustible.api.util.Method;

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
        protected Method<Object> prepare() {
            throw new RuntimeException();
        }
    };

    /**
     * Special prerequisite triggered when a retry attempt has been started.
     */
    public final static FeedPrerequisite<Object> RETRY_STARTED = new FeedPrerequisite<Object>() {
        @Override
        protected Method<Object> prepare() {
            throw new RuntimeException();
        }
    };

    /**
     * Special prerequisite triggered when an error has occurred.
     */
    public final static FeedPrerequisite<Object> ERROR = new FeedPrerequisite<Object>() {
        @Override
        protected Method<Object> prepare() {
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
         * @param isRefreshing True if this event is the result of a refresh, false otherwise
         */
        void onEvent(FeedPrerequisite<?> prerequisite, boolean isRefreshing);
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

    private void onEvent(FeedPrerequisite<?> prerequisite, boolean isRefreshing) {
        for (Listener listener : listeners) {
            listener.onEvent(prerequisite, isRefreshing);
        }
    }

    private final List<FeedPrerequisite<?>> pending = new ArrayList<>();
    private final List<FeedPrerequisite<?>> failed = new ArrayList<>();
    private final List<FeedPrerequisite<?>> successful = new ArrayList<>();

    private <T> void load(Connection connection, FeedPrerequisite<T> prerequisite, boolean isRefreshing) {
        connection.send(prerequisite.prepare(), t -> {
            prerequisite.value = t;
            if (isRefreshing) {
                assert successful.contains(prerequisite);
            } else {
                assert pending.contains(prerequisite);
                pending.remove(prerequisite);
                successful.add(prerequisite);
            }
            onEvent(prerequisite, isRefreshing);
            if (!isRefreshing && areLoaded()) {
                onEvent(COMPLETED, false);
            }
        }, () -> {
            if (isRefreshing) {
                // Ignore Errors When Prerequisite Has Previously Loaded
                assert successful.contains(prerequisite);
            } else {
                prerequisite.value = null;
                failed.add(prerequisite);
            }
            onEvent(ERROR, isRefreshing);
        });
    }

    /**
     * Add required prerequisite.
     * @param prerequisite The new prerequisite
     */
    public void add(FeedPrerequisite<?> prerequisite) {
        if (!isSetup) {
            pending.add(prerequisite);
        }
    }

    /**
     * Retry failed prerequisites.
     * @param connection The connection to Lemmy
     */
    public void retry(Connection connection) {
        assert !areLoaded() && isError();
        for (FeedPrerequisite<?> prerequisite : failed) {
            load(connection, prerequisite, false);
        }
        failed.clear();
        onEvent(RETRY_STARTED, false);
    }

    /**
     * Check if all prerequisites are loaded.
     * @return True if all prerequisites are loaded, false otherwise
     */
    public boolean areLoaded() {
        return pending.size() == 0;
    }

    /**
     * Check if the specified type of prerequisite is included.
     * @param klass The type of prerequisite to check for
     */
    public void require(Class<? extends FeedPrerequisite<?>> klass) {
        List<FeedPrerequisite<?>> allPrerequisites = new ArrayList<>();
        allPrerequisites.addAll(pending);
        allPrerequisites.addAll(successful);
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
        for (FeedPrerequisite<?> prerequisite : successful) {
            onEvent(prerequisite, false);
        }
        // Check If All Prerequisites Are Loaded
        if (areLoaded()) {
            // Prerequisites Are Already Loaded, Send Completion Event
            onEvent(COMPLETED, false);
        } else {
            // Restart Previous Prerequisites
            for (FeedPrerequisite<?> prerequisite : pending) {
                if (!failed.contains(prerequisite)) {
                    load(connection, prerequisite, false);
                }
            }
        }
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
        return failed.size() > 0;
    }

    /**
     * Refresh prerequisites.
     * @param connection The connection to Lemmy
     */
    public void refresh(Connection connection) {
        // Can't Refresh Anything If Loading Hasn't Finished
        if (!areLoaded()) {
            throw new RuntimeException();
        }

        // Refresh
        for (FeedPrerequisite<?> prerequisite : successful) {
            load(connection, prerequisite, true);
        }
    }
}
