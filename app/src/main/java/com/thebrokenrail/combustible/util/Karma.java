package com.thebrokenrail.combustible.util;

import android.content.Context;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.TooltipCompat;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.thebrokenrail.combustible.R;

/**
 * Utility class for managing upvotes and downvotes.
 */
public class Karma {
    /**
     * A callback for communicating the user's new vote with the server.
     */
    public interface VoteCallback {
        /**
         * Cast a vote.
         * @param score The new vote
         * @param successCallback Callback that is executed on success
         * @param errorCallback Callback that is executed on failure
         */
        void vote(int score, Runnable successCallback, Runnable errorCallback);

        /**
         * Store new vote in data structure.
         * @param score The new vote
         */
        void storeVote(int score);
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
    private Vote lastSuccessfulVote;
    private final VoteCallback callback;
    private final MaterialCheckBox upvote;
    private final MaterialCheckBox downvote;

    /**
     * Setup upvotes and downvotes on the provided view.
     * @param root The view to use
     * @param score The current total vote score
     * @param existingVote The user's current vote
     * @param canVote True if the user can vote, false otherwise
     */
    public Karma(View root, int score, int existingVote, boolean canVote, VoteCallback callback) {
        // Find Views
        upvote = root.findViewById(R.id.upvote);
        downvote = root.findViewById(R.id.downvote);
        TextView scoreView = root.findViewById(R.id.score);

        // Setup Buttons
        Context context = root.getContext();
        upvote.setContentDescription(context.getString(R.string.upvote));
        setupButton(upvote, canVote);
        upvote.setButtonDrawable(R.drawable.upvote);
        upvote.setButtonTintList(AppCompatResources.getColorStateList(context, R.color.upvote_color));
        downvote.setContentDescription(context.getString(R.string.downvote));
        setupButton(downvote, canVote);
        downvote.setButtonDrawable(R.drawable.downvote);
        downvote.setButtonTintList(AppCompatResources.getColorStateList(context, R.color.downvote_color));

        // Set Vote
        if (existingVote > 0) {
            vote = Vote.UPVOTE;
        } else if (existingVote < 0) {
            vote = Vote.DOWNVOTE;
        } else {
            vote = Vote.NEUTRAL;
        }
        lastSuccessfulVote = vote;
        updateVote();
        this.callback = callback;

        // Set Score
        scoreView.setText(String.valueOf(score));
    }

    private void setupButton(MaterialCheckBox button, boolean canVote) {
        button.setButtonIconDrawable(null);
        button.setText(null);
        button.setCenterIfNoTextEnabled(true);
        button.setMinHeight(0);
        button.setMinimumHeight(0);
        button.setMinWidth(0);
        button.setMinimumWidth(0);
        TooltipCompat.setTooltipText(button, button.getContentDescription());
        button.setEnabled(canVote);
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
    }

    private void sendVote() {
        int score = vote.getScore();
        callback.storeVote(score);
        callback.vote(score, () -> lastSuccessfulVote = vote, () -> {
            vote = lastSuccessfulVote;
            updateVote();
            callback.storeVote(vote.getScore());
            Util.unknownError(upvote.getContext());
        });
    }
}
