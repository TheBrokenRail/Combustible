package com.thebrokenrail.combustible.activity.feed.comment;

import androidx.annotation.Nullable;

import com.thebrokenrail.combustible.activity.feed.util.dataset.FeedDataset;
import com.thebrokenrail.combustible.api.method.CommentView;
import com.thebrokenrail.combustible.util.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CommentTreeDataset extends FeedDataset<CommentView> {
    private static class CommentData {
        private final CommentView view;
        private final int depth;
        private final Integer parent;
        private final List<Integer> realChildren = new ArrayList<>();

        private CommentData(CommentTreeDataset dataset, CommentView view, CommentData oldData) {
            this.view = view;
            depth = dataset.getDepth(view);

            // Find Parent
            if (depth > 0) {
                String[] path = view.comment.path.split("\\.");
                parent = Integer.parseInt(path[path.length - 2]);
            } else {
                parent = null;
            }

            // Copy Children From Previous Data
            if (oldData != null) {
                realChildren.addAll(oldData.realChildren);
            }
        }

        private int getTotalRealChildren(CommentTreeDataset dataset) {
            int total = realChildren.size();
            for (int childID : realChildren) {
                CommentData child = dataset.commentTree.get(childID);
                assert child != null;
                total += child.getTotalRealChildren(dataset);
            }
            return total;
        }
    }

    private final Map<Integer, CommentData> commentTree = new HashMap<>();
    private final List<CommentView> queuedComments = new ArrayList<>();
    private final List<CommentView> dataset = new ArrayList<>();

    public enum ParentType {
        POST,
        COMMENT
    }
    private ParentType parentType = ParentType.POST;
    private Integer parent = null;

    public void setup(ParentType parentType, Integer parent) {
        this.parentType = parentType;
        this.parent = parent;
    }

    private String getParentInPath() {
        if (parentType == ParentType.POST) {
            return "0";
        } else {
            return String.valueOf(parent);
        }
    }

    int getDepth(CommentView comment) {
        String[] path = comment.comment.path.split("\\.");
        int parentIndex = Arrays.asList(path).indexOf(getParentInPath());
        int depth = (path.length - 1) - parentIndex;
        if (parentType == ParentType.POST) {
            depth--;
        }
        assert depth >= 0;
        return depth;
    }

    @Override
    public int size() {
        return dataset.size();
    }

    @Override
    public CommentView get(int position) {
        return dataset.get(position);
    }

    @Override
    protected void addInternal(@Nullable Notifier notifier, List<CommentView> elements, boolean addToStart) {
        // Process Queue
        elements.addAll(0, queuedComments);
        queuedComments.clear();

        // Add To Tree
        for (CommentView comment : elements) {
            CommentData oldData = commentTree.get(comment.comment.id);
            CommentData data = new CommentData(this, comment, oldData);
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
                for (int i = 0; i < dataset.size(); i++) {
                    CommentView existingComment = dataset.get(i);
                    if (existingComment.comment.id.equals(comment.comment.id)) {
                        // Duplicate (Newer Comment Replaces Older)
                        isDuplicate = true;
                        dataset.set(i, comment);
                        if (notifier != null) {
                            // Notify RecyclerView
                            notifier.change(i, 1);
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
                    insertPosition = addToStart ? 0 : dataset.size();
                } else {
                    // Insert After Parent
                    CommentData commentParentData = commentTree.get(data.parent);
                    assert commentParentData != null;
                    insertPosition = dataset.indexOf(commentParentData.view);
                    assert insertPosition != -1;
                    insertPosition++;

                    // Insert After All Parent's Children
                    if (!addToStart) {
                        insertPosition += commentParentData.getTotalRealChildren(this);
                    }

                    // Add Child To Parent
                    commentParentData.realChildren.add(comment.comment.id);
                }
                dataset.add(insertPosition, comment);

                // Notify RecyclerView
                if (notifier != null) {
                    notifier.insert(insertPosition, 1);
                }
            }
        }
    }

    @Override
    public void clear(@Nullable Notifier notifier) {
        int size = size();
        dataset.clear();
        commentTree.clear();
        queuedComments.clear();
        if (notifier != null) {
            notifier.remove(0, size);
        }
    }

    @Override
    protected void replaceInternal(@Nullable Notifier notifier, CommentView oldElement, CommentView newElement) {
        // Update Tree
        for (Map.Entry<Integer, CommentData> entry : commentTree.entrySet()) {
            if (entry.getValue().view == oldElement) {
                assert Objects.equals(entry.getKey(), newElement.comment.id);
                CommentData data = new CommentData(this, newElement, entry.getValue());
                commentTree.put(entry.getKey(), data);
                break;
            }
        }
        // Update Dataset
        int index = dataset.indexOf(oldElement);
        if (index != -1) {
            dataset.set(index, newElement);
            if (notifier != null) {
                notifier.change(index, 1);
            }
        }
    }

    @Override
    public void remove(@Nullable Notifier notifier, CommentView element) {
        // Remove Children
        CommentData data = commentTree.get(element.comment.id);
        if (data != null) {
            List<Integer> realChildrenCopy = new ArrayList<>(data.realChildren);
            for (int child : realChildrenCopy) {
                CommentData childData = commentTree.get(child);
                assert childData != null;
                remove(notifier, childData.view);
            }
            assert data.realChildren.size() == 0;
            // Remove From Parent
            CommentData commentParentData = commentTree.get(data.parent);
            if (commentParentData != null) {
                commentParentData.realChildren.remove(element.comment.id);
            }
        }

        // Remove
        commentTree.remove(element.comment.id);
        int index = dataset.indexOf(element);
        if (index != -1) {
            dataset.remove(index);
            if (notifier != null) {
                notifier.remove(index, 1);
            }
        }
    }

    @Override
    protected boolean isBlocked(CommentView element) {
        // Invalid Comments
        if (element.comment.path.equals("0")) {
            return true;
        }

        // Depth Check
        int depth = getDepth(element);
        return depth >= Util.MAX_DEPTH;
    }

    @Override
    public int indexOf(CommentView element) {
        return dataset.indexOf(element);
    }

    public int getQueuedCommentsSize() {
        return queuedComments.size();
    }
}
