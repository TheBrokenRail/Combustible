package com.thebrokenrail.combustible.activity.settings.user;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;

import com.thebrokenrail.combustible.R;
import com.thebrokenrail.combustible.activity.settings.SettingsFragment;
import com.thebrokenrail.combustible.api.method.ListingType;
import com.thebrokenrail.combustible.api.method.SortType;

import java.util.Arrays;

public class UserSettingsFragment extends SettingsFragment {
    private UserSettingsDataStore dataStore;

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        dataStore = new ViewModelProvider(this).get(UserSettingsDataStore.ViewModel.class).dataStore;
        getPreferenceManager().setPreferenceDataStore(dataStore);
        setPreferencesFromResource(R.xml.user_settings, rootKey);
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View ret = super.onCreateView(inflater, container, savedInstanceState);

        // Load Settings
        UserSettingsActivity activity = (UserSettingsActivity) requireActivity();
        dataStore.setup(() -> {
            getPreferenceScreen().removeAll();
            addPreferencesFromResource(R.xml.user_settings);
            update();
        }, activity);
        dataStore.loadIfNeeded();
        update();

        // Return
        return ret;
    }

    private void update() {
        // Enabled
        for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++) {
            Preference preference = getPreferenceScreen().getPreference(i);
            preference.setEnabled(dataStore.isLoaded());
        }

        // Listing Types
        ListPreference defaultListingType = findPreference("default_listing_type");
        assert defaultListingType != null;
        defaultListingType.setEntryValues(Arrays.stream(ListingType.values()).map(Enum::name).toArray(String[]::new));
        // Sort Types
        ListPreference defaultSortType = findPreference("default_sort_type");
        assert defaultSortType != null;
        defaultSortType.setEntryValues(Arrays.stream(SortType.values()).map(Enum::name).toArray(String[]::new));

        // Multiline Biography
        EditTextPreference biography = findPreference("bio");
        assert biography != null;
        biography.setOnBindEditTextListener(editText -> {
            // Configure Biography EditText
            editText.setMaxLines(Integer.MAX_VALUE);
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        });

        // Email
        EditTextPreference email = findPreference("email");
        assert email != null;
        email.setOnBindEditTextListener(editText -> {
            // Configure Email EditText
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
            ViewCompat.setAutofillHints(editText, "emailAddress");
        });

        // Change Password
        Preference changePassword = findPreference("change_password");
        assert changePassword != null;
        changePassword.setOnPreferenceClickListener(preference -> {
            // Open Change Password Dialog
            new ChangePasswordDialogFragment().show(requireActivity().getSupportFragmentManager(), "change_password");
            return true;
        });

        // 2FA
        boolean hasTotp = dataStore.getTotpUrl() != null;
        Preference totpToggle = findPreference("totp_toggle");
        Preference totpUrl = findPreference("totp_url");
        assert totpToggle != null && totpUrl != null;
        if (hasTotp) {
            totpUrl.setVisible(true);
            totpToggle.setTitle(R.string.user_settings_totp_disable);
        } else {
            totpUrl.setVisible(false);
            totpToggle.setTitle(R.string.user_settings_totp_enable);
        }
        totpUrl.setOnPreferenceClickListener(preference -> {
            // Open TOTP URL
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(dataStore.getTotpUrl()));
            requireContext().startActivity(intent);
            return true;
        });
        totpToggle.setOnPreferenceClickListener(preference -> {
            // Toggle 2FA
            dataStore.putBoolean("generate_totp_2fa", !hasTotp);
            return true;
        });

        // Delete Account
        Preference deleteAccount = findPreference("delete_account");
        assert deleteAccount != null;
        deleteAccount.setOnPreferenceClickListener(preference -> {
            // Open Change Password Dialog
            new DeleteAccountDialogFragment().show(requireActivity().getSupportFragmentManager(), "delete_account");
            return true;
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();
        dataStore.setup(null, null);
    }
}
