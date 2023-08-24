package com.thebrokenrail.combustible.activity.feed.util.dataset;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SimpleFeedDataset<T> extends FeedDataset<T> {
    private final List<T> dataset = new ArrayList<>();

    @Override
    public int size() {
        return dataset.size();
    }

    @Override
    public T get(int position) {
        return dataset.get(position);
    }

    @Override
    protected void addInternal(@Nullable Notifier notifier, List<T> elements, boolean addToStart) {
        int insertPosition = addToStart ? 0 : dataset.size();
        dataset.addAll(insertPosition, elements);
        if (notifier != null) {
            notifier.insert(insertPosition, elements.size());
        }
    }

    @Override
    public void clear(@Nullable Notifier notifier) {
        int size = size();
        dataset.clear();
        if (notifier != null) {
            notifier.remove(0, size);
        }
    }

    @Override
    public void replaceInternal(@Nullable Notifier notifier, T oldElement, T newElement) {
        int index = dataset.indexOf(oldElement);
        if (index != -1) {
            dataset.set(index, newElement);
            if (notifier != null) {
                notifier.change(index);
            }
        }
    }

    @Override
    public void remove(@Nullable Notifier notifier, T element) {
        int index = dataset.indexOf(element);
        if (index != -1) {
            dataset.remove(index);
            if (notifier != null) {
                notifier.remove(index, 1);
            }
        }
    }

    @Override
    protected boolean isBlocked(T element) {
        return false;
    }

    @Override
    public int indexOf(T element) {
        return dataset.indexOf(element);
    }
}
