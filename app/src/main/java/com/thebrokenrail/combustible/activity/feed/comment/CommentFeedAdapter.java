package com.thebrokenrail.combustible.activity.feed.comment;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.thebrokenrail.combustible.api.Connection;
import com.thebrokenrail.combustible.api.method.CommentView;
import com.thebrokenrail.combustible.widget.DepthGauge;
import com.thebrokenrail.combustible.util.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CommentFeedAdapter extends FlatCommentFeedAdapter {
    public CommentFeedAdapter(View recyclerView, Connection connection, ViewModelProvider viewModelProvider, String viewModelKey, ParentType parentType, int parent) {
        super(recyclerView, connection, viewModelProvider, viewModelKey, parentType, parent);
    }

    @Override
    protected void bindElement(@NonNull RecyclerView.ViewHolder holder, int position) {
        super.bindElement(holder, position);
        CommentView obj = viewModel.dataset.get(position);
        CommentViewHolder commentViewHolder = (CommentViewHolder) holder;

        // Depth
        int depth = getDepth(obj);
        int previousDepth = -1;
        if (position > 0) {
            previousDepth = getDepth(viewModel.dataset.get(position - 1));
        }
        ((DepthGauge) commentViewHolder.itemView).setDepth(depth, previousDepth);

        // Show More
        if (obj.counts.child_count > 0 && depth == (Util.MAX_DEPTH - 1)) {
            commentViewHolder.showMore.setVisibility(View.VISIBLE);
            commentViewHolder.showMore.setOnClickListener(v -> commentViewHolder.card.callOnClick());
        } else {
            commentViewHolder.showMore.setVisibility(View.GONE);
        }

        // Card
        commentViewHolder.card.setClickable(false);
        commentViewHolder.card.setFocusable(false);
    }

    private String getParentInPath() {
        if (parentType == ParentType.POST) {
            return "0";
        } else {
            return String.valueOf(parent);
        }
    }

    private int getDepth(CommentView comment) {
        String[] path = comment.comment.path.split("\\.");
        int parentIndex = Arrays.asList(path).indexOf(getParentInPath());
        int depth = (path.length - 1) - parentIndex;
        if (parentType == ParentType.POST) {
            depth--;
        }
        return depth;
    }

    private static class CommentData {
        private final CommentView view;
        private final int depth;
        private final Integer parent;
        private final List<Integer> realChildren = new ArrayList<>();

        private CommentData(CommentFeedAdapter adapter, CommentView view) {
            this.view = view;
            depth = adapter.getDepth(view);

            // Find Parent
            if (depth > 0) {
                String[] path = view.comment.path.split("\\.");
                parent = Integer.parseInt(path[path.length - 2]);
            } else {
                parent = null;
            }
        }

        private int getTotalRealChildren(CommentFeedAdapter adapter) {
            int total = realChildren.size();
            for (int childID : realChildren) {
                CommentData child = adapter.commentTree.get(childID);
                assert child != null;
                total += child.getTotalRealChildren(adapter);
            }
            return total;
        }
    }
    protected final Map<Integer, CommentData> commentTree = new HashMap<>();

    protected final List<CommentView> queuedComments = new ArrayList<>();

    @Override
    protected void addElements(List<CommentView> elements, boolean manualNotifications) {
        // Copy For Modification
        elements = new ArrayList<>(elements);

        // Remove Comments Exceeding Maximum Depth
        elements.removeIf(commentView -> {
            int depth = getDepth(commentView);
            return depth >= Util.MAX_DEPTH;
        });

        // Remove Blocked Elements
        elements.removeIf(this::isBlocked);

        // Fix Broken Paths
        for (CommentView comment : elements) {
            if (comment.comment.path.equals("0")) {
                System.err.println("Bad Comment: " + comment.comment.id);
                comment.comment.path = "0." + comment.comment.id;
            }
        }

        // Process Queue
        elements.addAll(0, queuedComments);
        queuedComments.clear();

        // Add To Tree
        for (CommentView comment : elements) {
            CommentData data = new CommentData(this, comment);
            commentTree.put(comment.comment.id, data);
        }

        // Check If All Parent's Have Been Loaded
        elements.removeIf(commentView -> {
            int depth = getDepth(commentView);
            String[] path = commentView.comment.path.split("\\.");
            boolean allParentsLoaded = true;
            for (int i = 0; i < depth; i++) {
                int commentParentId = Integer.parseInt(path[path.length - i - 2]);
                if (!commentTree.containsKey(commentParentId)) {
                    // Parent Not Loaded
                    allParentsLoaded = false;
                    break;
                }
            }
            if (!allParentsLoaded) {
                // Queue For Later
                queuedComments.add(commentView);
                return true;
            } else {
                return false;
            }
        });

        // Find Minimum/Maximum Depth
        int minDepth = Integer.MAX_VALUE;
        int maxDepth = 0;
        for (CommentView comment : elements) {
            // Load Data
            CommentData data = commentTree.get(comment.comment.id);
            assert data != null;

            // Check Depth
            int depth = data.depth;
            maxDepth = Math.max(maxDepth, depth);
            minDepth = Math.min(minDepth, depth);
        }

        // Add To Dataset
        for (int targetDepth = minDepth; targetDepth <= maxDepth; targetDepth++) {
            Iterator<CommentView> it = elements.iterator();
            while (it.hasNext()) {
                // Load Data
                CommentView comment = it.next();
                CommentData data = commentTree.get(comment.comment.id);
                assert data != null;

                // Check Depth
                if (data.depth != targetDepth) {
                    // Skip
                    continue;
                } else {
                    it.remove();
                }

                // Check If Already Added To Dataset
                boolean isDuplicate = false;
                for (int i = 0; i < viewModel.dataset.size(); i++) {
                    CommentView existingComment = viewModel.dataset.get(i);
                    if (existingComment.comment.id.equals(comment.comment.id)) {
                        // Duplicate (Newer Comment Replaces Older)
                        isDuplicate = true;
                        viewModel.dataset.set(i, comment);
                        if (manualNotifications) {
                            // Notify RecyclerView
                            notifyItemChanged(getFirstElementPosition() + i);
                        }
                        break;
                    }
                }
                if (isDuplicate) {
                    continue;
                }

                // Add To Dataset
                int insertPosition;
                if (data.depth <= 0) {
                    // No Parent, Add To The End
                    insertPosition = viewModel.dataset.size();
                } else {
                    // Insert After Parent
                    CommentData commentParentData = commentTree.get(data.parent);
                    assert commentParentData != null;
                    insertPosition = viewModel.dataset.indexOf(commentParentData.view);
                    assert insertPosition != -1;
                    insertPosition++;

                    // Insert After All Parent's Children
                    insertPosition += data.getTotalRealChildren(this);

                    // Add Child To Parent
                    commentParentData.realChildren.add(comment.comment.id);
                }
                viewModel.dataset.add(insertPosition, comment);

                // Notify RecyclerView
                if (manualNotifications) {
                    notifyItemInserted(getFirstElementPosition() + insertPosition);
                }
            }
        }
    }

    @Override
    protected void clear() {
        super.clear();
        commentTree.clear();
        queuedComments.clear();
    }

    @Override
    protected void replace(CommentView oldElement, CommentView newElement) {
        if (!isBlocked(newElement)) {
            for (Map.Entry<Integer, CommentData> entry : commentTree.entrySet()) {
                if (entry.getValue().view == oldElement) {
                    assert Objects.equals(entry.getKey(), newElement.comment.id);
                    commentTree.put(entry.getKey(), new CommentData(this, newElement));
                    break;
                }
            }
        }
        super.replace(oldElement, newElement);
    }

    @Override
    protected void remove(CommentView element) {
        // Remove Children
        CommentData data = commentTree.get(element.comment.id);
        assert data != null;
        for (int child : data.realChildren) {
            CommentData childData = commentTree.get(child);
            assert childData != null;
            remove(childData.view);
        }

        // Remove
        commentTree.remove(element.comment.id);
        super.remove(element);
    }
}
