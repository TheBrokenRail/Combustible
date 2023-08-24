package com.thebrokenrail.combustible.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.TooltipCompat;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.thebrokenrail.combustible.R;
import com.thebrokenrail.combustible.util.Util;

/**
 * Utility class for managing upvotes and downvotes.
 */
public class Karma extends LinearLayout {
    /**
     * A callback for communicating the user's new vote with the server.
     */
    public interface VoteCallback {
        /**
         * Cast a vote.
         * @param score The new vote
         * @param errorCallback Callback that is executed on failure
         */
        void vote(int score, Runnable errorCallback);
    }

    private enum Vote {
        NEUTRAL,
        UPVOTE,
        DOWNVOTE;

        private int getScore() {
            if (this == Vote.UPVOTE) {
                return 1;
            } else if (this == Vote.DOWNVOTE) {
                return -1;
            } else {
                return 0;
            }
        }
    }

    private Vote vote;
    private Vote realVote = null;
    private VoteCallback callback = null;
    private final MaterialCheckBox upvote;
    private final AppCompatTextView scoreText;
    private final MaterialCheckBox downvote;
    private final ColorStateList upvoteColor;
    private final ColorStateList downvoteColor;

    public Karma(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        // Setup
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);

        // Margin
        int margin = getResources().getDimensionPixelSize(R.dimen.post_padding);

        // Upvote
        upvote = new MaterialCheckBox(getContext());
        LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        layoutParams.setMarginEnd(margin);
        upvote.setLayoutParams(layoutParams);
        upvote.setContentDescription(context.getString(R.string.upvote));
        setupButton(upvote);
        upvote.setButtonDrawable(R.drawable.upvote);
        upvoteColor = AppCompatResources.getColorStateList(context, R.color.upvote_color);
        upvote.setButtonTintList(upvoteColor);
        addView(upvote);

        // Score Text
        scoreText = new AppCompatTextView(getContext());
        layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        scoreText.setLayoutParams(layoutParams);
        addView(scoreText);

        // Downvote
        downvote = new MaterialCheckBox(getContext());
        layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        layoutParams.setMarginStart(margin);
        downvote.setLayoutParams(layoutParams);
        downvote.setContentDescription(context.getString(R.string.downvote));
        setupButton(downvote);
        downvote.setButtonDrawable(R.drawable.downvote);
        downvoteColor = AppCompatResources.getColorStateList(context, R.color.downvote_color);
        downvote.setButtonTintList(downvoteColor);
        addView(downvote);
    }

    /**
     * Setup the widget.
     * @param score The current total vote score
     * @param existingVote The user's current vote
     * @param canVote True if the user can vote, false otherwise
     * @param canDownvote True if downvotes are enabled, false otherwise
     * @param callback Voting callback
     */
    public void setup(int score, int existingVote, boolean canVote, boolean canDownvote, VoteCallback callback) {
        // Set Enabled/Disabled
        upvote.setEnabled(canVote);
        downvote.setEnabled(canVote && canDownvote);
        downvote.setVisibility(canDownvote ? View.VISIBLE : View.GONE);

        // Set Vote
        if (existingVote > 0) {
            vote = Vote.UPVOTE;
        } else if (existingVote < 0) {
            vote = Vote.DOWNVOTE;
        } else {
            vote = Vote.NEUTRAL;
        }
        realVote = vote;
        updateVote();
        this.callback = callback;

        // Set Score
        scoreText.setText(String.valueOf(score));
    }

    private void setupButton(MaterialCheckBox button) {
        button.setButtonIconDrawable(null);
        button.setText(null);
        button.setCenterIfNoTextEnabled(true);
        button.setMinHeight(0);
        button.setMinimumHeight(0);
        button.setMinWidth(0);
        button.setMinimumWidth(0);
        TooltipCompat.setTooltipText(button, button.getContentDescription());
        button.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isUpdatingVote) {
                vote = Vote.NEUTRAL;
                if (isChecked) {
                    if (button == upvote) {
                        vote = Vote.UPVOTE;
                    } else {
                        vote = Vote.DOWNVOTE;
                    }
                }
                updateVote();

                // Send Vote
                sendVote();
            }
        });
    }

    private boolean isUpdatingVote = false;

    private void updateVote() {
        isUpdatingVote = true;
        upvote.setChecked(vote == Vote.UPVOTE);
        downvote.setChecked(vote == Vote.DOWNVOTE);
        isUpdatingVote = false;

        // Text Color
        int textColor = upvoteColor.getDefaultColor();
        int[] checkedState = new int[]{android.R.attr.state_checked};
        if (vote == Vote.NEUTRAL) {
            textColor = upvoteColor.getColorForState(new int[]{}, textColor);
        } else if (vote == Vote.UPVOTE) {
            textColor = upvoteColor.getColorForState(checkedState, textColor);
        } else if (vote == Vote.DOWNVOTE) {
            textColor = downvoteColor.getColorForState(checkedState, textColor);
        }
        scoreText.setTextColor(textColor);
    }

    private void sendVote() {
        int score = vote.getScore();
        callback.vote(score, () -> {
            vote = realVote;
            updateVote();
            Util.unknownError(getContext());
        });
    }
}
