package com.thebrokenrail.combustible.util;

import android.view.View;
import android.widget.ImageView;

import com.thebrokenrail.combustible.R;

public class CommonIcons {
    private final ImageView deleted;
    private final ImageView locked;
    private final ImageView pinned;

    public CommonIcons(View root) {
        deleted = root.findViewById(R.id.icon_deleted);
        locked = root.findViewById(R.id.icon_locked);
        pinned = root.findViewById(R.id.icon_pinned);
    }

    private static void setVisible(ImageView image, boolean visible) {
        image.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public void setup(boolean isDeleted, boolean isLocked, boolean isPinned) {
        setVisible(deleted, isDeleted);
        setVisible(locked, isLocked);
        setVisible(pinned, isPinned);
    }
}
