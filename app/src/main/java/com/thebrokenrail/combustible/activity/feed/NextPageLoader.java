package com.thebrokenrail.combustible.activity.feed;

import android.annotation.SuppressLint;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.thebrokenrail.combustible.R;

@SuppressLint("ViewConstructor")
class NextPageLoader extends LinearLayout {
    final RecyclerView.ViewHolder viewHolder = new RecyclerView.ViewHolder(this) {};
    final FeedAdapter<?> adapter;

    public NextPageLoader(ViewGroup parent, FeedAdapter<?> adapter) {
        super(parent.getContext());
        this.adapter = adapter;
        RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT);
        int margin = parent.getContext().getResources().getDimensionPixelSize(R.dimen.feed_item_margin);
        layoutParams.setMargins(margin, margin, margin, 0);
        setLayoutParams(layoutParams);
        setGravity(Gravity.CENTER_HORIZONTAL);
        setOrientation(VERTICAL);
        setupProgress();
    }

    void setupProgress() {
        removeAllViews();

        // Create Progress Indicator
        CircularProgressIndicator progressIndicator = new CircularProgressIndicator(getContext());
        LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        progressIndicator.setLayoutParams(layoutParams);
        progressIndicator.setIndeterminate(true);
        addView(progressIndicator);
    }

    void setupError() {
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
        button.setOnClickListener(v -> adapter.load());
        addView(button);
    }
}
