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
import androidx.core.text.HtmlCompat;
import androidx.core.view.WindowCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.mikepenz.aboutlibraries.LibsBuilder;
import com.mikepenz.aboutlibraries.LibsConfiguration;
import com.mikepenz.aboutlibraries.entity.Library;
import com.mikepenz.aboutlibraries.entity.License;
import com.mikepenz.aboutlibraries.ui.LibsSupportFragment;
import com.mikepenz.aboutlibraries.util.SpecialButton;
import com.thebrokenrail.combustible.R;
import com.thebrokenrail.combustible.util.EdgeToEdge;
import com.thebrokenrail.combustible.util.Util;

public class LicensesActivity extends AppCompatActivity {
    private static class Listener implements LibsConfiguration.LibsListener {
        @Override
        public boolean onExtraClicked(@NonNull View view, @NonNull SpecialButton specialButton) {
            return false;
        }

        @Override
        public void onIconClicked(@NonNull View view) {
        }

        @Override
        public boolean onIconLongClicked(@NonNull View view) {
            return false;
        }

        @Override
        public boolean onLibraryAuthorClicked(@NonNull View view, @NonNull Library library) {
            return false;
        }

        @Override
        public boolean onLibraryAuthorLongClicked(@NonNull View view, @NonNull Library library) {
            return false;
        }

        @Override
        public boolean onLibraryBottomClicked(@NonNull View view, @NonNull Library library) {
            // Find License
            if (library.getLicenses().size() > 0) {
                License license = library.getLicenses().iterator().next();

                // Get Dialog Text
                CharSequence message = null;
                String licenseText = license.getLicenseContent();
                if (licenseText != null) {
                    // Make HTML-Friendly
                    licenseText = licenseText.replaceAll("\\n", "<br />");
                    // Parse
                    message = HtmlCompat.fromHtml(licenseText, HtmlCompat.FROM_HTML_MODE_LEGACY);
                }

                // Display Dialog
                if (message != null) {
                    Util.showTextDialog(Util.getActivityFromContext(view.getContext()), license.getName(), message);
                }
            }

            // Consume Event
            return true;
        }

        @Override
        public boolean onLibraryBottomLongClicked(@NonNull View view, @NonNull Library library) {
            return false;
        }

        @Override
        public boolean onLibraryContentClicked(@NonNull View view, @NonNull Library library) {
            return false;
        }

        @Override
        public boolean onLibraryContentLongClicked(@NonNull View view, @NonNull Library library) {
            return false;
        }
    }

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
                .withListener(new Listener())
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