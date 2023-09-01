package com.thebrokenrail.combustible.activity.settings.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.WindowCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.mikepenz.aboutlibraries.LibsBuilder;
import com.mikepenz.aboutlibraries.ui.LibsSupportFragment;
import com.thebrokenrail.combustible.R;
import com.thebrokenrail.combustible.util.EdgeToEdge;

public class LicensesActivity extends AppCompatActivity {
    public static class CustomLibsSupportFragment extends LibsSupportFragment {
        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            // Create View
            View view = super.onCreateView(inflater, container, savedInstanceState);

            // Custom Edge-To-Edge Handling
            assert view instanceof RecyclerView;
            EdgeToEdge.setupScroll(view);

            // Reset Scrollbar Style
            view.setScrollBarStyle(new View(requireContext()).getScrollBarStyle());

            // Return
            return view;
        }
    }

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
        actionBar.setTitle(R.string.app_settings_licenses);

        // Edge-To-Edge
        CoordinatorLayout root = findViewById(R.id.settings_root);
        EdgeToEdge.setupRoot(root);

        // Load
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.settings, createFragment()).commit();
        }
    }

    protected LibsSupportFragment createFragment() {
        LibsSupportFragment defaultFragment = new LibsBuilder()
                .withAboutMinimalDesign(true)
                .withLicenseDialog(true)
                .supportFragment();
        CustomLibsSupportFragment fragment = new CustomLibsSupportFragment();
        fragment.setArguments(defaultFragment.getArguments());
        return fragment;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}