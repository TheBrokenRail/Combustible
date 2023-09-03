package com.thebrokenrail.combustible.widget;

import android.database.DataSetObserver;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListAdapter;

class WrappedListAdapter implements ListAdapter, Filterable {
    private final ArrayAdapter<?> adapter;

    public WrappedListAdapter(ArrayAdapter<?> adapter) {
        assert adapter != null;
        this.adapter = adapter;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return adapter.areAllItemsEnabled();
    }

    @Override
    public boolean isEnabled(int position) {
        return adapter.isEnabled(position);
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        adapter.registerDataSetObserver(observer);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        adapter.unregisterDataSetObserver(observer);
    }

    @Override
    public int getCount() {
        return adapter.getCount();
    }

    @Override
    public Object getItem(int position) {
        return adapter.getItem(position);
    }

    @Override
    public long getItemId(int position) {
        return adapter.getItemId(position);
    }

    @Override
    public boolean hasStableIds() {
        return adapter.hasStableIds();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return adapter.getView(position, convertView, parent);
    }

    @Override
    public int getItemViewType(int position) {
        return adapter.getItemViewType(position);
    }

    @Override
    public int getViewTypeCount() {
        return adapter.getViewTypeCount();
    }

    @Override
    public boolean isEmpty() {
        return adapter.isEmpty();
    }

    @Override
    public CharSequence[] getAutofillOptions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return adapter.getAutofillOptions();
        } else {
            return null;
        }
    }

    @Override
    public Filter getFilter() {
        return adapter.getFilter();
    }
}
