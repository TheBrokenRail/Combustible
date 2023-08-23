package com.thebrokenrail.combustible.activity.fullscreen.welcome;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.thebrokenrail.combustible.R;
import com.thebrokenrail.combustible.activity.feed.post.PostFeedActivity;
import com.thebrokenrail.combustible.activity.fullscreen.FullscreenActivity;
import com.thebrokenrail.combustible.api.method.ListCommunities;
import com.thebrokenrail.combustible.util.Config;
import com.thebrokenrail.combustible.util.NiceLinkMovementMethod;
import com.thebrokenrail.combustible.util.Util;

import okhttp3.HttpUrl;

public class WelcomeActivity extends FullscreenActivity {
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
        NiceLinkMovementMethod.setup(explanation);

        // Handle Button Press
        Button go = findViewById(R.id.welcome_go);
        go.setOnClickListener(v -> {
            RadioButton recommendedInstanceOption = findViewById(R.id.welcome_instance_recommended);
            boolean isRecommendedInstance = recommendedInstanceOption.isChecked();
            if (isRecommendedInstance) {
                // Get Recommended Instance URL
                HttpUrl url = HttpUrl.parse(getString(R.string.recommended_instance));

                // Check
                checkAndSetInstance(url);
            } else {
                // Custom Instance
                new CustomInstanceDialogFragment().show(getSupportFragmentManager(), "custom_instance");
            }
        });
    }

    void checkAndSetInstance(HttpUrl instance) {
        connect(instance);

        // Test Instance
        connection.send(new ListCommunities(), response -> {
            // Success

            // Set Instance URL
            Config config = new Config(WelcomeActivity.this);
            config.setInstance(instance);

            // Finish Setup
            finishSetup();
        }, () -> {
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
