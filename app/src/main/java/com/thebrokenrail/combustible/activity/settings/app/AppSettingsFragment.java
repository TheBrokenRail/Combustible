package com.thebrokenrail.combustible.activity.settings.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;

import androidx.preference.Preference;

import com.thebrokenrail.combustible.BuildConfig;
import com.thebrokenrail.combustible.R;
import com.thebrokenrail.combustible.activity.SubApplication;
import com.thebrokenrail.combustible.activity.fullscreen.welcome.WelcomeActivity;
import com.thebrokenrail.combustible.activity.settings.SettingsFragment;
import com.thebrokenrail.combustible.util.SearchSuggestionProvider;
import com.thebrokenrail.combustible.util.config.Config;

import java.util.Objects;

public class AppSettingsFragment extends SettingsFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.app_settings, rootKey);

        // Change Instance
        Preference changeInstance = findPreference("change_instance");
        assert changeInstance != null;
        changeInstance.setOnPreferenceClickListener(preference -> {
            Intent intent = new Intent(requireContext(), WelcomeActivity.class);
            startActivity(intent);
            return true;
        });
        changeInstance.setSummary(Config.create(requireContext()).getInstance().toString());

        // Clear Search History
        Preference clearSearchHistory = findPreference("clear_search_history");
        assert clearSearchHistory != null;
        clearSearchHistory.setOnPreferenceClickListener(preference -> {
            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(requireContext(), SearchSuggestionProvider.AUTHORITY, SearchSuggestionProvider.MODE);
            suggestions.clearHistory();
            return true;
        });

        // App Version
        Preference version = findPreference("version");
        assert version != null;
        version.setSummary(BuildConfig.VERSION_NAME);

        // Open Source Licenses
        Preference licenses = findPreference("licenses");
        assert licenses != null;
        licenses.setOnPreferenceClickListener(preference -> {
            Context context = requireContext();
            Intent intent = new Intent(context, LicensesActivity.class);
            context.startActivity(intent);
            return true;
        });

        // Update Media Settings
        updateMediaSettings();
    }

    private void updateMediaSettings() {
        Preference disableLargeThumbnail = findPreference(AppSettings.DISABLE_LARGE_THUMBNAIL.key);
        assert disableLargeThumbnail != null;
        Preference disableMarkdownImages = findPreference(AppSettings.DISABLE_MARKDOWN_IMAGES.key);
        assert disableMarkdownImages != null;
        // Update
        boolean disableImages = AppSettings.DISABLE_IMAGES.getBool(requireContext());
        disableLargeThumbnail.setEnabled(!disableImages);
        disableMarkdownImages.setEnabled(!disableImages);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        assert key != null;
        if (key.equals(AppSettings.DARK_MODE.key)) {
            SubApplication.setDarkMode(getContext());
        } else if (key.equals(AppSettings.DISABLE_LARGE_THUMBNAIL.key) || key.equals(AppSettings.DISABLE_IMAGES.key) || key.equals(AppSettings.DISABLE_MARKDOWN_IMAGES.key)) {
            ((AppSettingsActivity) requireActivity()).recreateOtherActivities();
            if (key.equals(AppSettings.DISABLE_IMAGES.key)) {
                updateMediaSettings();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Objects.requireNonNull(getPreferenceManager().getSharedPreferences()).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        Objects.requireNonNull(getPreferenceManager().getSharedPreferences()).unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }
}
