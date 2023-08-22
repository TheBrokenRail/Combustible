package com.thebrokenrail.combustible.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.thebrokenrail.combustible.R;

/**
 * Widget that shows a progress indicator for the loading of the next page.
 */
public class NextPageLoader extends FrameLayout {
    public enum DisplayMode {
        /**
         * Show a progress indicator.
         */
        PROGRESS,
        /**
         * Show an error message with a retry button.
         */
        ERROR,
        /**
         * Show nothing.
         */
        NONE
    }

    private final LinearLayout inner;

    public NextPageLoader(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        inner = new LinearLayout(context);
        LayoutParams layoutParams = new LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT);
        int margin = getResources().getDimensionPixelSize(R.dimen.feed_item_margin);
        layoutParams.setMargins(margin, margin, margin, 0);
        inner.setLayoutParams(layoutParams);
        inner.setGravity(Gravity.CENTER_HORIZONTAL);
        inner.setOrientation(LinearLayout.VERTICAL);
        addView(inner);
        setupProgress();
    }

    /**
     * Setup widget.
     * @param displayMode The widget's new mode
     * @param retry Callback that is executed when the retry button is clicked
     */
    public void setup(DisplayMode displayMode, Runnable retry) {
        if (displayMode == DisplayMode.NONE) {
            inner.setVisibility(GONE);
        } else {
            inner.setVisibility(VISIBLE);
            if (displayMode == DisplayMode.PROGRESS) {
                setupProgress();
            } else if (displayMode == DisplayMode.ERROR) {
                setupError(retry);
            } else {
                throw new RuntimeException();
            }
        }
    }

    private void setupProgress() {
        inner.removeAllViews();

        // Create Progress Indicator
        CircularProgressIndicator progressIndicator = new CircularProgressIndicator(getContext());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        progressIndicator.setLayoutParams(layoutParams);
        progressIndicator.setIndeterminate(true);
        inner.addView(progressIndicator);
    }

    private void setupError(Runnable retry) {
        removeAllViews();

        // Create TextView
        TextView text = new TextView(getContext());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        text.setLayoutParams(layoutParams);
        text.setTextAlignment(TEXT_ALIGNMENT_CENTER);
        text.setText(R.string.feed_error);
        inner.addView(text);

        // Create Retry Button
        Button button = new MaterialButton(getContext());
        int margin = getResources().getDimensionPixelSize(R.dimen.feed_item_margin);
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, margin, 0, 0);
        button.setLayoutParams(layoutParams);
        button.setText(R.string.feed_retry);
        button.setOnClickListener(v -> retry.run());
        inner.addView(button);
    }
}
