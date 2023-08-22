package com.thebrokenrail.combustible.activity.fullscreen;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.thebrokenrail.combustible.R;
import com.thebrokenrail.combustible.activity.feed.post.PostFeedActivity;
import com.thebrokenrail.combustible.api.Connection;
import com.thebrokenrail.combustible.api.method.ListCommunities;
import com.thebrokenrail.combustible.util.Config;
import com.thebrokenrail.combustible.util.NiceLinkMovementMethod;
import com.thebrokenrail.combustible.util.Util;

import java.util.Objects;

import okhttp3.HttpUrl;

public class WelcomeActivity extends FullscreenActivity {
    private Button go = null;
    private boolean isFirstSetup = true;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // Load Configuration
        Config config = new Config(this);
        isFirstSetup = !config.isSetup();

        // Make Link Clickable
        TextView explanation = findViewById(R.id.welcome_explanation);
        explanation.setMovementMethod(NiceLinkMovementMethod.getInstance());

        // Handle Button Press
        go = findViewById(R.id.welcome_go);
        go.setOnClickListener(v -> {
            go.setEnabled(false);
            RadioButton recommendedInstanceOption = findViewById(R.id.welcome_instance_recommended);
            boolean isRecommendedInstance = recommendedInstanceOption.isChecked();
            if (isRecommendedInstance) {
                // Get Recommended Instance URL
                HttpUrl url = HttpUrl.parse(getString(R.string.recommended_instance));

                // Check
                checkAndSetInstance(url);
            } else {
                // Create Dialog View
                LayoutInflater inflater = LayoutInflater.from(this);
                View root = inflater.inflate(R.layout.dialog_custom_instance, null);

                // Create Dialog
                AlertDialog dialog = new MaterialAlertDialogBuilder(WelcomeActivity.this)
                        .setTitle(R.string.welcome_instance_custom)
                        .setView(root)
                        .setPositiveButton(R.string.ok, null)
                        .setNegativeButton(R.string.cancel, null)
                        .setOnDismissListener(dialog1 -> go.setEnabled(true))
                        .show();
                TextInputLayout urlField = root.findViewById(R.id.dialog_custom_instance_field);
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v12 -> {
                    String urlStr = Objects.requireNonNull(urlField.getEditText()).getText().toString();

                    // Parse
                    HttpUrl url = HttpUrl.parse(urlStr);
                    if (url == null) {
                        // Error
                        urlField.setError(getString(R.string.welcome_invalid_url));
                        return;
                    }

                    // Dismiss
                    dialog.dismiss();

                    // Check
                    checkAndSetInstance(url);
                });
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(v1 -> dialog.dismiss());
                urlField.requestFocus();
            }
        });
    }

    private void checkAndSetInstance(HttpUrl instance) {
        Connection connection = new Connection(instance);
        connection.setCallbackHelper(this::runOnUiThread);

        // Test Instance
        connection.send(new ListCommunities(), response -> {
            // Success

            // Set Instance URL
            Config config = new Config(WelcomeActivity.this);
            config.setInstance(instance);

            // Finish Setup
            finishSetup();
        }, () -> {
            // Error
            go.setEnabled(true);

            // Error Dialog
            Util.showTextDialog(WelcomeActivity.this, R.string.error, R.string.welcome_error);
        });
    }

    private void finishSetup() {
        // Mark Completed
        Config config = new Config(this);
        config.finishSetup();

        // Replace Activity
        if (isFirstSetup) {
            Intent intent = new Intent(this, PostFeedActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
            startActivity(intent);
        }
        finish();
    }
}
