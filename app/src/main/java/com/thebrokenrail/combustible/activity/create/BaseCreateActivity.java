package com.thebrokenrail.combustible.activity.create;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.ActionBar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.WindowCompat;
import androidx.core.widget.NestedScrollView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.thebrokenrail.combustible.R;
import com.thebrokenrail.combustible.activity.LemmyActivity;
import com.thebrokenrail.combustible.api.util.Method;
import com.thebrokenrail.combustible.util.EdgeToEdge;
import com.thebrokenrail.combustible.util.Util;

public abstract class BaseCreateActivity<T, K> extends LemmyActivity {
    public static final String EDIT_ID_EXTRA = "com.thebrokenrail.combustible.EDIT_ID_EXTRA";

    public static final String WAS_EDIT_KEY = "wasEdit";
    public static final String OBJ_KEY = "obj";

    protected int editId;

    protected boolean isEditing = false;
    private boolean loaded = true;

    protected TextInputEditText title;
    protected TextInputEditText url;
    protected TextInputEditText body;
    protected MaterialCheckBox nsfw;

    protected TextInputLayout titleField;
    protected TextInputLayout urlField;
    protected TextInputLayout bodyField;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_create_post);

        // Toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);

        // Grab Views
        title = findViewById(R.id.create_post_title_edit_text);
        url = findViewById(R.id.create_post_url_edit_text);
        body = findViewById(R.id.create_post_body_edit_text);
        nsfw = findViewById(R.id.create_post_nsfw);
        titleField = findViewById(R.id.create_post_title_field);
        urlField = findViewById(R.id.create_post_url_field);
        bodyField = findViewById(R.id.create_post_body_field);

        // State
        isEditing = getIntent().hasExtra(EDIT_ID_EXTRA);
        if (savedInstanceState != null) {
            loaded = savedInstanceState.getBoolean("loaded");
        } else {
            loaded = !isEditing;
        }
        updateFields();

        // Get IDs
        editId = getIntent().getIntExtra(EDIT_ID_EXTRA, -1);

        // Title
        actionBar.setTitle(getActionBarTitle());

        // Load Post If Editing
        if (isEditing && !loaded) {
            Method<T> method = loadExisting();
            connection.send(method, obj -> {
                // Set Fields
                setFieldsFromExisting(obj);

                // Enable Fields
                loaded = true;
                updateFields();
            }, () -> {
                // Error
                Util.unknownError(BaseCreateActivity.this);
            });
        }

        // Edge-To-Edge
        NestedScrollView scroll = findViewById(R.id.create_post_scroll);
        EdgeToEdge.setupScroll(scroll);
        CoordinatorLayout root = findViewById(R.id.create_post_root);
        EdgeToEdge.setupRoot(root);
    }

    protected abstract @StringRes int getActionBarTitle();

    protected abstract Method<T> loadExisting();
    protected abstract void setFieldsFromExisting(T existing);

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("loaded", loaded);
    }

    private void updateFields() {
        titleField.setEnabled(loaded);
        urlField.setEnabled(loaded);
        bodyField.setEnabled(loaded);
        nsfw.setEnabled(loaded);
        invalidateOptionsMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.create_post_toolbar, menu);
        return true;
    }

    protected abstract void go();

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean ret = super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.create_post_go).setEnabled(loaded);
        return ret;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        } else if (item.getItemId() == R.id.create_post_go) {
            go();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    protected abstract Class<K> getResponseClass();

    protected void success(K obj) {
        Intent intent = new Intent();
        intent.putExtra(WAS_EDIT_KEY, isEditing);
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<K> jsonAdapter = moshi.adapter(getResponseClass());
        intent.putExtra(OBJ_KEY, jsonAdapter.toJson(obj));
        setResult(RESULT_OK, intent);
        finish();
    }
}
