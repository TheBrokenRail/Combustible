package com.thebrokenrail.combustible.activity.feed.comment;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.thebrokenrail.combustible.R;
import com.thebrokenrail.combustible.activity.feed.FeedAdapter;
import com.thebrokenrail.combustible.api.Connection;
import com.thebrokenrail.combustible.api.method.CommentSortType;
import com.thebrokenrail.combustible.api.method.CommentView;
import com.thebrokenrail.combustible.api.method.CreateCommentLike;
import com.thebrokenrail.combustible.api.method.GetComments;
import com.thebrokenrail.combustible.api.method.PostView;
import com.thebrokenrail.combustible.util.CommonIcons;
import com.thebrokenrail.combustible.util.DepthGauge;
import com.thebrokenrail.combustible.util.Karma;
import com.thebrokenrail.combustible.util.LinkWithIcon;
import com.thebrokenrail.combustible.util.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

class CommentFeedAdapter extends FeedAdapter<CommentView> {
    private static class CommentViewHolder extends RecyclerView.ViewHolder {
        private final TextView text;
        private final View karma;
        private final LinkWithIcon creator;
        private final ImageView overflow;
        private final CommonIcons icons;
        private final Button showMore;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.comment_text);
            karma = itemView.findViewById(R.id.comment_karma);
            creator = itemView.findViewById(R.id.comment_creator);
            overflow = itemView.findViewById(R.id.comment_overflow);
            icons = new CommonIcons(itemView.findViewById(R.id.comment_icons));
            showMore = itemView.findViewById(R.id.comment_show_more);
        }
    }

    private CommentSortType sortBy = CommentSortType.Hot;

    enum ParentType {
        POST,
        COMMENT
    }
    private final ParentType parentType;
    private final int parent;

    private PostView parentPost = null;

    public CommentFeedAdapter(RecyclerView recyclerView, Connection connection, ParentType parentType, int parent) {
        super(recyclerView, connection);
        this.parentType = parentType;
        this.parent = parent;
    }

    @Override
    public boolean hasHeader() {
        return true;
    }

    @Override
    public View createHeader(ViewGroup parent) {
        // Inflate Layout
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View root = inflater.inflate(R.layout.comment_header, parent, false);

        // Setup Sort Spinner
        Spinner spinner = root.findViewById(R.id.comments_sort_by_spinner);
        int textViewResId = android.R.layout.simple_spinner_item;
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(parent.getContext(), R.array.comment_sort_types, textViewResId);
        int dropDownViewResource = android.R.layout.simple_spinner_dropdown_item;
        adapter.setDropDownViewResource(dropDownViewResource);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sortBy = CommentSortType.values()[position];
                refresh(true, () -> {});
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Return
        return root;
    }

    @Override
    protected void bindHeader(View root) {
        // Sort Spinner
        Spinner spinner = root.findViewById(R.id.comments_sort_by_spinner);
        spinner.setSelection(sortBy.ordinal());
    }

    @Override
    public RecyclerView.ViewHolder createItem(ViewGroup parent) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View root = inflater.inflate(R.layout.comment, parent, false);
        return new CommentViewHolder(root);
    }

    @Override
    protected void bindElement(@NonNull RecyclerView.ViewHolder holder, int position) {
        CommentView obj = dataset.get(position);
        CommentViewHolder commentViewHolder = (CommentViewHolder) holder;

        // Title
        commentViewHolder.text.setText(obj.comment.content);

        // Karma
        new Karma(commentViewHolder.karma, obj.counts.score, obj.my_vote != null ? obj.my_vote : 0, connection.hasToken(), new Karma.VoteCallback() {
            @Override
            public void vote(int score, Runnable successCallback, Runnable errorCallback) {
                CreateCommentLike method = new CreateCommentLike();
                method.comment_id = obj.comment.id;
                method.score = score;
                connection.send(method, commentResponse -> successCallback.run(), errorCallback);
            }

            @Override
            public void storeVote(int score) {
                obj.my_vote = score;
            }
        });

        // Comment Creator
        commentViewHolder.creator.setup(obj.creator.avatar, Util.getPersonName(obj.creator), () -> {
            // TODO
        });

        // Overflow
        commentViewHolder.overflow.setOnClickListener(v -> {
        });

        // Icons
        commentViewHolder.icons.setup(obj.comment.deleted || obj.comment.removed, false, obj.comment.distinguished);

        // Depth
        int depth = getDepth(obj);
        int previousDepth = -1;
        if (position > 0) {
            previousDepth = getDepth(dataset.get(position - 1));
        }
        ((DepthGauge) commentViewHolder.itemView).setDepth(depth, previousDepth);

        // Show More
        if (obj.counts.child_count > 0 && depth == (Util.MAX_DEPTH - 1)) {
            commentViewHolder.showMore.setVisibility(View.VISIBLE);
            commentViewHolder.showMore.setOnClickListener(v -> {
                Context context = v.getContext();
                Intent intent = new Intent(context, CommentFeedActivity.class);
                intent.putExtra(CommentFeedActivity.COMMENT_ID_EXTRA, obj.comment.id);
                context.startActivity(intent);
            });
        } else {
            commentViewHolder.showMore.setVisibility(View.GONE);
        }
    }

    @Override
    protected void loadPage(int page, Consumer<List<CommentView>> successCallback, Runnable errorCallback) {
        GetComments method = new GetComments();
        method.page = page;
        method.limit = Util.ELEMENTS_PER_PAGE;
        method.max_depth = Util.MAX_DEPTH;
        method.sort = sortBy;
        if (parentType == ParentType.POST) {
            method.post_id = parent;
        } else {
            method.parent_id = parent;
        }
        connection.send(method, getCommentsResponse -> successCallback.accept(getCommentsResponse.comments), errorCallback);
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
        private int depth = 0;
        private CommentData parent = null;
        private final List<CommentData> realChildren = new ArrayList<>();

        private CommentData(CommentView view) {
            this.view = view;
        }

        private CommentData getLastRealChild() {
            if (realChildren.size() > 0) {
                return realChildren.get(realChildren.size() - 1);
            } else {
                return null;
            }
        }
    }
    private final Map<Integer, CommentData> commentTree = new HashMap<>();

    private final List<CommentView> queuedComments = new ArrayList<>();

    @Override
    protected void addElements(List<CommentView> elements, boolean manualNotifications) {
        // Process Queue
        elements.addAll(0, queuedComments);
        queuedComments.clear();

        // Add To Tree
        for (CommentView comment : elements) {
            if (!commentTree.containsKey(comment.comment.id)) {
                CommentData data = new CommentData(comment);
                commentTree.put(comment.comment.id, data);
            }
        }

        // Attach Children To Parents
        int minDepth = Integer.MAX_VALUE;
        int maxDepth = 0;
        for (CommentView comment : elements) {
            // Load Data
            CommentData data = commentTree.get(comment.comment.id);
            assert data != null;

            // Check Depth
            int depth = getDepth(comment);
            maxDepth = Math.max(maxDepth, depth);
            minDepth = Math.min(minDepth, depth);
            if (depth != 0) {
                // Find Parent
                String[] path = comment.comment.path.split("\\.");
                int commentParentId = Integer.parseInt(path[path.length - 2]);
                CommentData commentParentData = commentTree.get(commentParentId);
                if (commentParentData == null) {
                    // Parent Comment Isn't Loaded Yet
                    queuedComments.add(comment);
                    continue;
                }

                // Add Parent To Child
                data.parent = commentParentData;
            }
            data.depth = depth;
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
                if (data.depth != targetDepth || queuedComments.contains(comment)) {
                    // Skip
                    continue;
                } else {
                    it.remove();
                }

                // Add To Dataset
                int insertPosition;
                if (data.depth == 0) {
                    // No Parent, Add To The End
                    insertPosition = dataset.size();
                } else {
                    // Find Parent
                    CommentData commentParentData = data.parent;
                    if (commentParentData == null) {
                        // Parent Does Not Exist Yet
                        continue;
                    }

                    // Find Insert Position
                    CommentView insertAfter;
                    if (commentParentData.getLastRealChild() != null) {
                        // Insert After Last Child Of Parent (Transitively)
                        CommentData temp = commentParentData;
                        while (true) {
                            CommentData newTemp = temp.getLastRealChild();
                            if (newTemp.getLastRealChild() == null) {
                                break;
                            } else {
                                temp = newTemp;
                            }
                        }
                        insertAfter = temp.getLastRealChild().view;
                        assert dataset.contains(insertAfter);
                    } else {
                        // Insert After Parent
                        insertAfter = commentParentData.view;
                    }
                    int insertAfterIndex = dataset.indexOf(insertAfter);
                    if (insertAfterIndex == -1) {
                        // Parent Comment Isn't Loaded Yet
                        queuedComments.add(comment);
                        continue;
                    }
                    insertPosition = insertAfterIndex + 1;
                    commentParentData.realChildren.add(commentTree.get(comment.comment.id));
                }
                dataset.add(insertPosition, comment);

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
}
