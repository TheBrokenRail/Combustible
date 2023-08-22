package com.thebrokenrail.combustible.util;

import android.app.Dialog;
import android.content.Context;
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

    public static void unknownError(Context context) {
        Toast.makeText(context.getApplicationContext(), R.string.unknown_error, Toast.LENGTH_SHORT).show();
    }

    public static class TextDialogFragment extends AppCompatDialogFragment {
        private TextDialogFragment(@StringRes int title, @StringRes int text) {
            super();
            Bundle arguments = new Bundle();
            arguments.putInt("title", title);
            arguments.putInt("text", text);
            setArguments(arguments);
        }

        public TextDialogFragment() {
            super();
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            Bundle arguments = getArguments();
            assert arguments != null;
            return new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(arguments.getInt("title"))
                    .setMessage(arguments.getInt("text"))
                    .setNeutralButton(R.string.ok, null)
                    .create();
        }
    }

    public static void showTextDialog(AppCompatActivity context, @StringRes int title, @StringRes int text) {
        TextDialogFragment dialog = new TextDialogFragment(title, text);
        dialog.show(context.getSupportFragmentManager(), "text_dialog_" + title + ":" + text);
    }

    public static void updateAppBarLift(AppBarLayout appBarLayout, View target) {
        appBarLayout.setLiftOnScrollTargetView(target);
        appBarLayout.setLifted(target.canScrollVertically(-1) || target.getScrollY() > 0);
    }
}
