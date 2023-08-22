package com.thebrokenrail.combustible.activity.settings.user;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceDataStore;

import com.thebrokenrail.combustible.R;
import com.thebrokenrail.combustible.api.Connection;
import com.thebrokenrail.combustible.api.method.GetSite;
import com.thebrokenrail.combustible.api.method.LocalUserView;
import com.thebrokenrail.combustible.api.method.SaveUserSettings;
import com.thebrokenrail.combustible.util.Util;

import java.lang.reflect.Field;
import java.util.AbstractMap;
import java.util.Map;

public class UserSettingsDataStore extends PreferenceDataStore {
    public static class ViewModel extends androidx.lifecycle.ViewModel {
        UserSettingsDataStore dataStore = new UserSettingsDataStore();
    }

    private LocalUserView user = null;
    boolean isLoaded() {
        return user != null;
    }
    String getTotpUrl() {
        return isLoaded() ? user.local_user.totp_2fa_url : null;
    }

    private Runnable reloadCallback;
    private UserSettingsActivity activity;
    void setup(Runnable reloadCallback, UserSettingsActivity activity) {
        this.reloadCallback = reloadCallback;
        this.activity = activity;
    }

    void loadIfNeeded() {
        Connection connection = activity.getConnection();
        if (connection.hasToken()) {
            if (!isLoaded()) {
                GetSite method = new GetSite();
                connection.send(method, getSiteResponse -> {
                    if (getSiteResponse.my_user != null) {
                        user = getSiteResponse.my_user.local_user_view;
                    }
                    reloadCallback.run();
                }, () -> Util.unknownError(activity));
            }
        } else {
            user = null;
        }
    }

    private Map.Entry<Object, Field> getUserField(String key) {
        try {
            Object obj;
            Field field;
            try {
                obj = user.local_user;
                field = obj.getClass().getField(key);
            } catch (NoSuchFieldException e) {
                obj = user.person;
                field = obj.getClass().getField(key);
            }
            return new AbstractMap.SimpleImmutableEntry<>(obj, field);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings({"unchecked"})
    private void setField(Object obj, Field field, Object value) {
        try {
            if (field.getType().isEnum()) {
                // Handle Enums
                Enum<?>[] constants = ((Class<Enum<?>>) field.getType()).getEnumConstants();
                assert constants != null;
                for (Enum<?> constant : constants) {
                    if (constant.name().equals(value)) {
                        field.set(obj, constant);
                        break;
                    }
                }
            } else {
                field.set(obj, value);
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private <T> void set(String key, T value) {
        // Check
        if (!isLoaded()) {
            return;
        }

        // Create Method
        SaveUserSettings method = new SaveUserSettings();

        // Workaround https://github.com/LemmyNet/lemmy-js-client/issues/144
        method.show_nsfw = user.local_user.show_nsfw;
        method.bot_account = user.person.bot_account;

        // Set Field
        try {
            Field field = method.getClass().getField(key);
            setField(method, field, value);

            // Send
            Connection connection = activity.getConnection();
            connection.send(method, loginResponse -> {
                // Make Other Activities Refresh
                activity.triggerRefresh();

                // 2FA
                boolean isTotp = key.equals("generate_totp_2fa");
                if (isTotp) {
                    activity.fullRecreate();
                }

                // Update User Object
                if (!isTotp) {
                    Map.Entry<Object, Field> userField = getUserField(key);
                    setField(userField.getKey(), userField.getValue(), value);
                }

                // Verification Email
                if (key.equals("email")) {
                    Util.showTextDialog(activity, R.string.feed_menu_user_settings, R.string.user_settings_verification_email_sent);
                }
            }, () -> {
                Util.unknownError(activity);
                reloadCallback.run();
            });
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings({"unchecked"})
    private <T> T get(String key, T defValue) {
        if (!isLoaded()) {
            return defValue;
        }
        T value;
        try {
            Map.Entry<Object, Field> userField = getUserField(key);
            Object obj = userField.getKey();
            Field field = userField.getValue();
            if (field.getType().isEnum()) {
                Enum<?> constant = (Enum<?>) field.get(obj);
                if (constant != null) {
                    value = (T) constant.name();
                } else {
                    value = null;
                }
            } else {
                value = (T) field.get(obj);
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        if (value == null) {
            return defValue;
        } else {
            return value;
        }
    }

    @Override
    public boolean getBoolean(String key, boolean defValue) {
        return get(key, false);
    }

    @Override
    public void putBoolean(String key, boolean value) {
        set(key, value);
    }

    @Nullable
    @Override
    public String getString(String key, @Nullable String defValue) {
        return get(key, defValue);
    }

    @Override
    public void putString(String key, @Nullable String value) {
        set(key, value);
    }
}
