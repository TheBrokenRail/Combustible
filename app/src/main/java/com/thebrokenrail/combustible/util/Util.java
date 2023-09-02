package com.thebrokenrail.combustible.util;

import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.thebrokenrail.combustible.R;

public class Util {
    public static final int ELEMENTS_PER_PAGE = 40;
    public static final int MAX_DEPTH = 8;
    public static final int MIN_LIMIT = 1; // Limit Cannot Be 0

    public static void unknownError(Context context) {
        Toast.makeText(context.getApplicationContext(), R.string.unknown_error, Toast.LENGTH_SHORT).show();
    }

    public static boolean isDarkMode(Context context) {
        int currentNightMode = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        switch (currentNightMode) {
            case Configuration.UI_MODE_NIGHT_NO:
            case Configuration.UI_MODE_NIGHT_UNDEFINED:
                return false;
            case Configuration.UI_MODE_NIGHT_YES:
                return true;
        }
        return false;
    }

    public static class TextDialogFragment extends AppCompatDialogFragment {
        private TextDialogFragment(CharSequence title, CharSequence text) {
            super();
            Bundle arguments = new Bundle();
            arguments.putCharSequence("title", title);
            arguments.putCharSequence("text", text);
            setArguments(arguments);
        }

        public TextDialogFragment() {
            super();
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            // Create Dialog
            Bundle arguments = getArguments();
            assert arguments != null;
            return new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(arguments.getCharSequence("title"))
                    .setMessage(arguments.getCharSequence("text"))
                    .setNeutralButton(R.string.ok, null)
                    .create();
        }
    }

    public static void showTextDialog(AppCompatActivity context, CharSequence title, CharSequence text) {
        TextDialogFragment dialog = new TextDialogFragment(title, text);
        dialog.show(context.getSupportFragmentManager(), "text_dialog_" + title.hashCode() + ":" + text.hashCode());
    }

    public static void showTextDialog(AppCompatActivity context, @StringRes int title, @StringRes int text) {
        showTextDialog(context, context.getString(title), context.getString(text));
    }

    public static void updateAppBarLift(AppBarLayout appBarLayout, View target) {
        appBarLayout.setLiftOnScrollTargetView(target);
        appBarLayout.setLifted(target.canScrollVertically(-1) || target.getScrollY() > 0);
    }

    public static String getThumbnailUrl(String iconUrl) {
        return iconUrl + "?thumbnail=128" + (iconUrl.endsWith(".jpeg") ? "&format=jpg" : "");
    }

    public static AppCompatActivity getActivityFromContext(@NonNull Context context){
        while (context instanceof ContextWrapper) {
            if (context instanceof AppCompatActivity) {
                return (AppCompatActivity) context;
            } else {
                context = ((ContextWrapper) context).getBaseContext();
            }
        }
        throw new RuntimeException();
    }
}
