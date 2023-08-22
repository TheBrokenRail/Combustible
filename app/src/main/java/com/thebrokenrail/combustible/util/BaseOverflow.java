package com.thebrokenrail.combustible.util;

import android.content.Context;
import android.view.Menu;
import android.view.View;

import androidx.appcompat.widget.PopupMenu;

import androidx.annotation.MenuRes;

import com.thebrokenrail.combustible.api.Connection;

import java.util.function.Consumer;

public abstract class BaseOverflow<T> implements PopupMenu.OnMenuItemClickListener {
    protected final Context context;
    protected final Connection connection;
    protected final T obj;
    protected final Consumer<T> updateFunction;

    public BaseOverflow(View view, Connection connection, T obj, Consumer<T> updateFunction) {
        this.context = view.getContext();
        this.connection = connection;
        this.obj = obj;
        this.updateFunction = updateFunction;

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
}
