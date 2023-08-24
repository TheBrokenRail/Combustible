package com.thebrokenrail.combustible.activity.feed.util.overflow;

import android.content.Context;
import android.view.Menu;
import android.view.View;

import androidx.annotation.MenuRes;
import androidx.appcompat.widget.PopupMenu;

import com.thebrokenrail.combustible.api.Connection;

public abstract class BaseOverflow<T> implements PopupMenu.OnMenuItemClickListener {
    protected final Context context;
    protected final Connection connection;
    protected final T obj;

    public BaseOverflow(View view, Connection connection, T obj) {
        this.context = view.getContext();
        this.connection = connection;
        this.obj = obj;

        // Show
        PopupMenu popup = new PopupMenu(context, view);
        popup.inflate(getMenuResource());
        onCreateMenu(popup.getMenu());
        popup.setOnMenuItemClickListener(this);
        popup.setForceShowIcon(true);
        popup.show();
    }

    protected abstract @MenuRes int getMenuResource();

    protected abstract void onCreateMenu(Menu menu);

    protected abstract void update(T newObj);
}
