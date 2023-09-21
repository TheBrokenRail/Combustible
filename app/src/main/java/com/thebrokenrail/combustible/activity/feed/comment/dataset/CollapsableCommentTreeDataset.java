package com.thebrokenrail.combustible.activity.feed.comment.dataset;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.thebrokenrail.combustible.api.method.CommentView;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link CommentTreeDataset} with support for collapsing comment trees.
 */
public class CollapsableCommentTreeDataset extends CommentTreeDataset {
    private final List<Integer> collapsed = new ArrayList<>();

    @Override
    public void clear(@Nullable Notifier notifier) {
        collapsed.clear();
        super.clear(notifier);
    }

    /**
     * Collapse (or un-collapse) comment.
     * @param notifier Callback to notify a {@link RecyclerView.Adapter} (or null to disable notification)
     * @param comment The comment to collapse
     */
    public void collapse(@Nullable Notifier notifier, CommentView comment) {
        // Collapse Comment
        if (collapsed.contains(comment.comment.id)) {
            // Already Collapsed, Un-collapse
            collapsed.remove(comment.comment.id);
        } else {
            // Collapse
            collapsed.add(comment.comment.id);
        }
        // Notify
        if (notifier != null) {
            int index = indexOf(comment);
            if (index != -1) {
                int totalToChange = 1;
                CommentData data = commentTree.get(comment.comment.id);
                if (data != null) {
                    totalToChange += data.getTotalRealChildren(this);
                }
                notifier.change(index, totalToChange);
            }
        }
    }

    /**
     * Check if comment is visible.
     * @param comment The comment to check
     * @return True if it is visible, false otherwise
     */
    public boolean isVisible(CommentView comment) {
        // Check If A Parent Is Collapsed
        for (Integer collapsedId : collapsed) {
            if (comment.comment.path.contains("." + collapsedId + ".")) {
                // Invisible
                return false;
            }
        }
        // Visible
        return true;
    }

    /**
     * Check if comment is collapsed.
     * @param comment The comment to check
     * @return True if it is collapsed, false otherwise
     */
    public boolean isCollapsed(CommentView comment) {
        // Check If Comment Is Collapsed
        for (Integer collapsedId : collapsed) {
            if (comment.comment.id.equals(collapsedId)) {
                // Collapsed
                return true;
            }
        }
        // Not Collapsed
        return false;
    }
}
