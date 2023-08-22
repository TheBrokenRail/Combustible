package com.thebrokenrail.combustible.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

import com.google.android.material.R;
import com.google.android.material.button.MaterialButton;

/**
 * Button that may be outlined or filled.
 */
public class PossiblyOutlinedButton extends FrameLayout {
    private final MaterialButton filled;
    private final MaterialButton outlined;

    public PossiblyOutlinedButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        // Filled Button
        filled = new MaterialButton(context, null, R.attr.materialButtonStyle);
        LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        filled.setLayoutParams(layoutParams);
        addView(filled);

        // Outlined Button
        outlined = new MaterialButton(context, null, R.attr.materialButtonOutlinedStyle);
        layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        outlined.setLayoutParams(layoutParams);
        addView(outlined);

        // Setup
        setOutlined(false);
    }

    /**
     * Set if button is outlined.
     * @param isOutlined True if the button should be outlined, false otherwise
     */
    public void setOutlined(boolean isOutlined) {
        outlined.setVisibility(isOutlined ? VISIBLE : GONE);
        filled.setVisibility(isOutlined ? GONE : VISIBLE);
    }

    /**
     * Set button text.
     * @param text The new text
     */
    public void setText(String text) {
        outlined.setText(text);
        filled.setText(text);
    }

    /**
     * Set button on-click listener.
     * @param l Callback that is executed when the button is clicked
     */
    public void setButtonOnClickListener(OnClickListener l) {
        outlined.setOnClickListener(l);
        filled.setOnClickListener(l);
    }
}
