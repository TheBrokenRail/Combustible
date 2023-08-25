package com.thebrokenrail.combustible.activity.feed.comment;

import android.content.Context;
import android.content.Intent;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.thebrokenrail.combustible.R;
import com.thebrokenrail.combustible.activity.create.CommentCreateActivity;
import com.thebrokenrail.combustible.activity.feed.util.dataset.FeedDataset;
import com.thebrokenrail.combustible.activity.feed.util.prerequisite.FeedPrerequisite;
import com.thebrokenrail.combustible.activity.feed.util.prerequisite.FeedPrerequisites;
import com.thebrokenrail.combustible.api.Connection;
import com.thebrokenrail.combustible.api.method.CommentSortType;
import com.thebrokenrail.combustible.api.method.CommentView;
import com.thebrokenrail.combustible.api.method.GetComments;
import com.thebrokenrail.combustible.api.method.ListingType;
import com.thebrokenrail.combustible.util.RequestCodes;
import com.thebrokenrail.combustible.util.Util;
import com.thebrokenrail.combustible.widget.DepthGauge;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class CommentFeedAdapter extends BaseCommentFeedAdapter {
    private final CommentTreeDataset.ParentType parentType;
    private final int parent;

    private Integer postId = null;

    public CommentFeedAdapter(View recyclerView, Connection connection, ViewModelProvider viewModelProvider, CommentTreeDataset.ParentType parentType, int parent) {
        super(recyclerView, connection, viewModelProvider);
        ((CommentTreeDataset) viewModel.dataset).setup(parentType, parent);
        this.parentType = parentType;
        this.parent = parent;
    }

    @Override
    protected FeedDataset<CommentView> createDataset() {
        return new CommentTreeDataset();
    }

    @Override
    protected boolean showCreator() {
        return true;
    }

    @Override
    protected boolean showCommunity() {
        return false;
    }

    @Override
    protected void bindHeader(View root) {
        super.bindHeader(root);

        // Header Button
        AppCompatButton headerButton = root.findViewById(R.id.comments_header_button);
        headerButton.setVisibility(View.VISIBLE);
        if (parentType == CommentTreeDataset.ParentType.COMMENT) {
            // View All
            headerButton.setText(R.string.comment_view_all);
            headerButton.setEnabled(postId != null);
            if (headerButton.isEnabled()) {
                headerButton.setOnClickListener(v -> {
                    Context context = v.getContext();
                    Intent intent = new Intent(context, CommentFeedActivity.class);
                    intent.putExtra(CommentFeedActivity.POST_ID_EXTRA, postId);
                    context.startActivity(intent);
                });
            }
        } else {
            // Reply
            headerButton.setText(R.string.reply);
            headerButton.setEnabled(post != null && site != null && permissions.canReply(post.post_view.post));
            if (headerButton.isEnabled()) {
                headerButton.setOnClickListener(v -> {
                    AppCompatActivity activity = Util.getActivityFromContext(v.getContext());
                    Intent intent = new Intent(activity, CommentCreateActivity.class);
                    intent.putExtra(CommentFeedActivity.POST_ID_EXTRA, post.post_view.post.id);
                    //noinspection deprecation
                    activity.startActivityForResult(intent, RequestCodes.CREATE_COMMENT);
                });
            }
        }
    }

    @Override
    protected void bindElement(@NonNull RecyclerView.ViewHolder holder, int position) {
        super.bindElement(holder, position);
        CommentView obj = viewModel.dataset.get(position);
        CommentViewHolder commentViewHolder = (CommentViewHolder) holder;

        // Depth
        CommentTreeDataset dataset = (CommentTreeDataset) viewModel.dataset;
        int depth = dataset.getDepth(obj);
        ((DepthGauge) commentViewHolder.itemView).setDepth(depth, () -> {
            // Get Current Position
            int newPosition = dataset.indexOf(obj);
            if (newPosition == -1) {
                // Invalid State
                return -1;
            }

            // Get Previous Item's Depth
            int previousDepth1 = -1;
            if (newPosition > 0) {
                previousDepth1 = dataset.getDepth(viewModel.dataset.get(newPosition - 1));
            }

            // Return
            return previousDepth1;
        });

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

        // Reply
        boolean canReply = permissions.canReply(post != null ? post.post_view.post : obj.post);
        commentViewHolder.reply.setVisibility(View.VISIBLE);
        commentViewHolder.reply.setEnabled(canReply);
        if (commentViewHolder.reply.isEnabled()) {
            commentViewHolder.reply.setImageAlpha(255);
            commentViewHolder.reply.setOnClickListener(v -> {
                AppCompatActivity activity = Util.getActivityFromContext(v.getContext());
                Intent intent = new Intent(activity, CommentCreateActivity.class);
                intent.putExtra(CommentFeedActivity.POST_ID_EXTRA, obj.post.id);
                intent.putExtra(CommentFeedActivity.COMMENT_ID_EXTRA, obj.comment.id);
                //noinspection deprecation
                activity.startActivityForResult(intent, RequestCodes.CREATE_COMMENT);
            });
        } else {
            // Disabled Appearance
            TypedValue typedValue = new TypedValue();
            commentViewHolder.itemView.getContext().getResources().getValue(com.google.android.material.R.dimen.material_emphasis_disabled, typedValue, true);
            float alpha = typedValue.getFloat();
            commentViewHolder.reply.setImageAlpha((int) (alpha * 255));
        }
    }

    @Override
    protected void handlePrerequisites(FeedPrerequisites prerequisites) {
        super.handlePrerequisites(prerequisites);
        boolean isPost = parentType == CommentTreeDataset.ParentType.POST;
        if (isPost) {
            prerequisites.require(FeedPrerequisite.Post.class);
        } else {
            prerequisites.require(FeedPrerequisite.Comment.class);
        }
        prerequisites.listen(prerequisite -> {
            if (prerequisite instanceof FeedPrerequisite.Site) {
                // Reload Header
                notifyItemChanged(0);
            } else if (isPost && prerequisite instanceof FeedPrerequisite.Post) {
                // Show The Post Itself
                post = ((FeedPrerequisite.Post) prerequisite).get();
                // Wait Until Site Has Loaded
                if (site != null) {
                    // Reload Header
                    notifyItemChanged(0);
                }
            } else if (!isPost && prerequisite instanceof FeedPrerequisite.Comment) {
                // View All Button
                postId = ((FeedPrerequisite.Comment) prerequisite).get().comment_view.post.id;
                // Reload Header
                notifyItemChanged(0);
            }
        });
    }

    @Override
    protected void loadPage(int page, Consumer<List<CommentView>> successCallback, Runnable errorCallback) {
        GetComments method = new GetComments();
        method.page = page;
        method.limit = Util.ELEMENTS_PER_PAGE;
        method.sort = sorting.get(CommentSortType.class);
        if (parentType == CommentTreeDataset.ParentType.POST) {
            method.post_id = parent;
        } else {
            method.parent_id = parent;
        }
        method.type_ = ListingType.All;
        connection.send(method, getCommentsResponse -> successCallback.accept(getCommentsResponse.comments), errorCallback);
    }

    @Override
    protected boolean isSortingTypeVisible(Class<? extends Enum<?>> type) {
        return type == CommentSortType.class;
    }

    @Override
    protected List<Class<? extends FeedPrerequisite<?>>> getPrerequisitesToRefresh() {
        if (parentType == CommentTreeDataset.ParentType.POST) {
            return Collections.singletonList(FeedPrerequisite.Post.class);
        } else {
            return super.getPrerequisitesToRefresh();
        }
    }
}
