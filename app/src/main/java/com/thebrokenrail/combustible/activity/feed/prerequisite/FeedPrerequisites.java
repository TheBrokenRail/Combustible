package com.thebrokenrail.combustible.activity.feed.prerequisite;

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

    private <T> void load(Connection connection, FeedPrerequisite<T> prerequisite) {
        connection.send(prerequisite.prepare(), t -> {
            prerequisite.value = t;
            assert pendingPrerequisites.contains(prerequisite);
            pendingPrerequisites.remove(prerequisite);
            successfulPrerequisites.add(prerequisite);
            onEvent(prerequisite);
            if (areLoaded()) {
                onEvent(COMPLETED);
            }
        }, () -> {
            prerequisite.value = null;
            failedPrerequisites.add(prerequisite);
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
    public void require(Class<?> klass) {
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
        for (FeedPrerequisite<?> prerequisite : successfulPrerequisites) {
            onEvent(prerequisite);
        }
        if (areLoaded()) {
            onEvent(COMPLETED);
        } else {
            for (FeedPrerequisite<?> prerequisite : pendingPrerequisites) {
                if (!failedPrerequisites.contains(prerequisite)) {
                    load(connection, prerequisite);
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
        return failedPrerequisites.size() > 0;
    }
}
