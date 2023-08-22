package com.thebrokenrail.combustible.activity.fullscreen;

import android.os.Bundle;
import android.widget.ScrollView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.thebrokenrail.combustible.R;

class FullscreenActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Edge-To-Edge
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);

        // Edge-To-Edge
        ScrollView scrollView = findViewById(R.id.fullscreen_scroll_view);
        ViewCompat.setOnApplyWindowInsetsListener(scrollView, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemGestures());
            scrollView.setPadding(insets.left, insets.top, insets.right, insets.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
    }
}
