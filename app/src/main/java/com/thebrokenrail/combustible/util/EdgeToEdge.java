package com.thebrokenrail.combustible.util;

import android.view.View;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class EdgeToEdge {
    public static void setupRoot(View root) {
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            root.setPadding(insets.left, 0, insets.right, 0);
            return windowInsets;
        });
    }

    public static void setupScroll(View scroll) {
        ViewCompat.setOnApplyWindowInsetsListener(scroll, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            scroll.setPadding(0, 0, 0, insets.bottom);
            return windowInsets;
        });
    }
}
