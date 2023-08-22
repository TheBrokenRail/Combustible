package com.thebrokenrail.combustible.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.thebrokenrail.combustible.R;

/**
 * Widget that shows a progress indicator for the loading of the next page.
 */
public class NextPageLoader extends LinearLayout {
    public NextPageLoader(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setGravity(Gravity.CENTER_HORIZONTAL);
        setOrientation(VERTICAL);
        setupProgress();
    }

    /**
     * Show a progress indicator.
     */
    public void setupProgress() {
        removeAllViews();

        // Create Progress Indicator
        CircularProgressIndicator progressIndicator = new CircularProgressIndicator(getContext());
        LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        progressIndicator.setLayoutParams(layoutParams);
        progressIndicator.setIndeterminate(true);
        addView(progressIndicator);
    }

    /**
     * Show an error message with a retry button.
     * @param retry Callback that is executed when the retry button is clicked
     */
    public void setupError(Runnable retry) {
        removeAllViews();

        // Create TextView
        TextView text = new TextView(getContext());
        LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        text.setLayoutParams(layoutParams);
        text.setTextAlignment(TEXT_ALIGNMENT_CENTER);
        text.setText(R.string.feed_error);
        addView(text);

        // Create Retry Button
        Button button = new MaterialButton(getContext());
        button.setLayoutParams(layoutParams);
        button.setText(R.string.feed_retry);
        button.setOnClickListener(v -> retry.run());
        addView(button);
    }
}
