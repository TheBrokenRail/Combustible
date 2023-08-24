package com.thebrokenrail.combustible.activity.feed.comment;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.thebrokenrail.combustible.R;
import com.thebrokenrail.combustible.activity.feed.util.dataset.FeedDataset;
import com.thebrokenrail.combustible.activity.feed.util.prerequisite.FeedPrerequisite;
import com.thebrokenrail.combustible.activity.feed.util.prerequisite.FeedPrerequisites;
import com.thebrokenrail.combustible.api.Connection;
import com.thebrokenrail.combustible.api.method.CommentSortType;
import com.thebrokenrail.combustible.api.method.CommentView;
import com.thebrokenrail.combustible.api.method.GetComments;
import com.thebrokenrail.combustible.api.method.ListingType;
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

        // View All
        AppCompatButton viewAll = root.findViewById(R.id.comments_view_all);
        if (parentType == CommentTreeDataset.ParentType.COMMENT) {
            viewAll.setVisibility(View.VISIBLE);
            viewAll.setEnabled(postId != null);
            if (viewAll.isEnabled()) {
                viewAll.setOnClickListener(v -> {
                    Context context = v.getContext();
                    Intent intent = new Intent(context, CommentFeedActivity.class);
                    intent.putExtra(CommentFeedActivity.POST_ID_EXTRA, postId);
                    context.startActivity(intent);
                });
            }
        } else {
            viewAll.setVisibility(View.GONE);
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
            assert newPosition != -1;

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
