package com.thebrokenrail.combustible.util;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.StringRes;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.thebrokenrail.combustible.R;
import com.thebrokenrail.combustible.activity.fullscreen.LoginActivity;
import com.thebrokenrail.combustible.api.Connection;
import com.thebrokenrail.combustible.api.method.Community;
import com.thebrokenrail.combustible.api.method.Person;

import okhttp3.HttpUrl;

public class Util {
    public static final int ELEMENTS_PER_PAGE = 40;
    public static final int MAX_DEPTH = 6;

    public static void unknownError(Context context) {
        Toast.makeText(context.getApplicationContext(), R.string.unknown_error, Toast.LENGTH_SHORT).show();
    }

    public static String getCommunityName(Community community) {
        String name = community.title;;
        if (!community.local) {
            HttpUrl url = HttpUrl.parse(community.actor_id);
            if (url != null) {
                name += '@' + url.host();
            }
        }
        return name;
    }

    public static String getPersonName(Person person) {
        if (person.display_name != null) {
            return person.display_name;
        } else {
            String name = '@' + person.name;
            if (!person.local) {
                HttpUrl url = HttpUrl.parse(person.actor_id);
                if (url != null) {
                    name += '@' + url.host();
                }
            }
            return name;
        }
    }

    public static void showTextDialog(Context context, @StringRes int title, @StringRes int text) {
        new MaterialAlertDialogBuilder(context)
                .setTitle(title)
                .setMessage(text)
                .setNeutralButton(R.string.ok, null)
                .show();
    }
}
