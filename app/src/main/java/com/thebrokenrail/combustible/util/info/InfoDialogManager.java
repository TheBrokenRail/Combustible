package com.thebrokenrail.combustible.util.info;

import androidx.appcompat.app.AppCompatActivity;

import com.thebrokenrail.combustible.api.Connection;

import java.util.HashMap;
import java.util.Map;

/**
 * Information dialog manager.
 */
public class InfoDialogManager {
    private final AppCompatActivity context;
    private final Connection connection;

    final Map<String, InfoDialog> dialogs = new HashMap<>();

    public InfoDialogManager(AppCompatActivity context, Connection connection) {
        this.context = context;
        this.connection = connection;
    }

    /**
     * Create dialog.
     * @param key A unique key
     * @return The new dialog
     */
    public InfoDialog create(String key) {
        key = "info_" + key;
        InfoDialog dialog = new InfoDialog(context, connection, key);
        dialogs.put(key, dialog);
        return dialog;
    }
}
