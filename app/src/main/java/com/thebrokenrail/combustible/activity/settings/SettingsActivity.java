package com.thebrokenrail.combustible.activity.settings;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.WindowCompat;
import androidx.preference.EditTextPreference;
import androidx.preference.EditTextPreferenceDialogFragmentCompat;
import androidx.preference.ListPreference;
import androidx.preference.ListPreferenceDialogFragmentCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.thebrokenrail.combustible.R;
import com.thebrokenrail.combustible.activity.LemmyActivity;
import com.thebrokenrail.combustible.util.Config;
import com.thebrokenrail.combustible.util.EdgeToEdge;

public abstract class SettingsActivity extends LemmyActivity implements PreferenceFragmentCompat.OnPreferenceDisplayDialogCallback {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_settings);

        // Toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);

        // Edge-To-Edge
        CoordinatorLayout root = findViewById(R.id.settings_root);
        EdgeToEdge.setupRoot(root);

        // Load
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.settings, createFragment()).commit();
        }
    }

    protected abstract PreferenceFragmentCompat createFragment();

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    // https://stackoverflow.com/a/74112704/16198887
    public static class MaterialListPreference extends ListPreferenceDialogFragmentCompat {
        private MaterialListPreference(Preference preference) {
            super();
            Bundle bundle = new Bundle(1);
            bundle.putString(ARG_KEY, preference.getKey());
            setArguments(bundle);
        }

        public MaterialListPreference() {
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireActivity())
                    .setTitle(getPreference().getTitle())
                    .setPositiveButton(getPreference().getPositiveButtonText(), this)
                    .setNegativeButton(getPreference().getNegativeButtonText(), this);
            View root = onCreateDialogView(requireActivity());
            if (root == null) {
                builder.setMessage(getPreference().getDialogMessage());
            } else {
                onBindDialogView(root);
                builder.setView(root);
            }
            onPrepareDialogBuilder(builder);
            return builder.create();
        }
    }

    public static class MaterialEditTextPreference extends EditTextPreferenceDialogFragmentCompat {
        private MaterialEditTextPreference(Preference preference) {
            super();
            Bundle bundle = new Bundle(1);
            bundle.putString(ARG_KEY, preference.getKey());
            setArguments(bundle);
        }

        public MaterialEditTextPreference() {
        }

        @SuppressLint("InflateParams")
        @Nullable
        @Override
        protected View onCreateDialogView(@NonNull Context context) {
            return getLayoutInflater().inflate(R.layout.dialog_preference, null);
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireActivity())
                    .setTitle(getPreference().getTitle())
                    .setPositiveButton(getPreference().getPositiveButtonText(), this)
                    .setNegativeButton(getPreference().getNegativeButtonText(), this);
            View root = onCreateDialogView(requireActivity());
            assert root != null;
            onBindDialogView(root);
            builder.setView(root);
            onPrepareDialogBuilder(builder);
            return builder.create();
        }
    }

    @Override
    public boolean onPreferenceDisplayDialog(@NonNull PreferenceFragmentCompat caller, @NonNull Preference pref) {
        if (pref instanceof ListPreference) {
            MaterialListPreference dialogFragment = new MaterialListPreference(pref);
            //noinspection deprecation
            dialogFragment.setTargetFragment(caller, 0);
            dialogFragment.show(getSupportFragmentManager(), dialogFragment.toString());
            return true;
        } else if (pref instanceof EditTextPreference) {
            MaterialEditTextPreference dialogFragment = new MaterialEditTextPreference(pref);
            //noinspection deprecation
            dialogFragment.setTargetFragment(caller, 0);
            dialogFragment.show(getSupportFragmentManager(), dialogFragment.toString());
            return true;
        } else {
            return false;
        }
    }

    public void triggerRefresh() {
        Config config = new Config(this);
        config.triggerRefresh();
        acknowledgeConfigChange();
    }
}