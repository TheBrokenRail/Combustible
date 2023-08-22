package com.thebrokenrail.combustible.activity.feed.util.dataset;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class FeedDataset<T> implements Iterable<T> {
    /**
     * Callback to notify a {@link RecyclerView.Adapter} of dataset changes.
     */
    public interface Notifier {
        void insert(int position, int amount);
        void remove(int position, int amount);
        void change(int position);
    }

    /**
     * Get the size of the dataset.
     * @return The dataset's size
     */
    public abstract int size();

    /**
     * Get element from dataset.
     * @param position Element position/index
     * @return The element
     */
    public abstract T get(int position);

    /**
     * Add the provided elements to this adapter.
     * @param notifier Callback to notify a {@link RecyclerView.Adapter} (or null to disable notification)
     * @param elements The new elements
     * @param addToStart True if the elements should be added to the dataset's start, false otherwise
     */
    public final void add(@Nullable Notifier notifier, List<T> elements, boolean addToStart) {
        // Remove Blocked Elements
        elements = new ArrayList<>(elements);
        elements.removeIf(this::isBlocked);

        // Add
        addInternal(notifier, elements, addToStart);
    }
    protected abstract void addInternal(@Nullable Notifier notifier, List<T> elements, boolean addToStart);

    /**
     * Remove all elements from adapter.
     * @param notifier Callback to notify a {@link RecyclerView.Adapter} (or null to disable notification)
     */
    public abstract void clear(@Nullable Notifier notifier);

    /**
     * Replace the specified element with a new one.
     * @param notifier Callback to notify a {@link RecyclerView.Adapter} (or null to disable notification)
     * @param oldElement The old element to be replaced
     * @param newElement The new element (may be same as oldElement)
     */
    public final void replace(@Nullable Notifier notifier, T oldElement, T newElement) {
        if (newElement == null || isBlocked(newElement)) {
            // Remove Element
            remove(notifier, oldElement);
        } else {
            // Replace Element
            replaceInternal(notifier, oldElement, newElement);
        }
    }
    protected abstract void replaceInternal(@Nullable Notifier notifier, T oldElement, T newElement);

    /**
     * Remove the specified element.
     * @param notifier Callback to notify a {@link RecyclerView.Adapter} (or null to disable notification)
     * @param element The element to remove
     */
    public abstract void remove(@Nullable Notifier notifier, T element);

    /**
     * Check if an element is blocked.
     * @param element The element to check
     * @return True if the element is blocked, false otherwise
     */
    protected abstract boolean isBlocked(T element);

    @NonNull
    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < size();
            }

            @Override
            public T next() {
                return get(index++);
            }
        };
    }
}
