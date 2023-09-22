package com.thebrokenrail.combustible.activity.feed.comment;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.thebrokenrail.combustible.R;
import com.thebrokenrail.combustible.activity.feed.post.BasePostFeedAdapter;
import com.thebrokenrail.combustible.activity.feed.post.PostContext;
import com.thebrokenrail.combustible.activity.feed.util.adapter.SortableFeedAdapter;
import com.thebrokenrail.combustible.api.Connection;
import com.thebrokenrail.combustible.api.method.CommentView;
import com.thebrokenrail.combustible.api.method.CreateCommentLike;
import com.thebrokenrail.combustible.api.method.GetPostResponse;
import com.thebrokenrail.combustible.api.method.GetSiteResponse;
import com.thebrokenrail.combustible.api.method.PostView;
import com.thebrokenrail.combustible.util.Images;
import com.thebrokenrail.combustible.util.Permissions;
import com.thebrokenrail.combustible.util.markdown.Markdown;
import com.thebrokenrail.combustible.widget.DepthGauge;
import com.thebrokenrail.combustible.widget.Karma;
import com.thebrokenrail.combustible.widget.PostOrCommentHeader;

import java.util.Collections;

public abstract class BaseCommentFeedAdapter extends SortableFeedAdapter<CommentView> {
    protected static class CommentViewHolder extends RecyclerView.ViewHolder {
        protected final CardView card;
        private final TextView text;
        private final Karma karma;
        private final PostOrCommentHeader header;
        protected final Button showMore;
        protected final AppCompatImageView reply;
        protected final DepthGauge depthGauge;

        private CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.comment_card);
            text = itemView.findViewById(R.id.comment_text);
            karma = itemView.findViewById(R.id.comment_karma);
            header = itemView.findViewById(R.id.comment_header);
            showMore = itemView.findViewById(R.id.comment_show_more);
            reply = itemView.findViewById(R.id.comment_reply);
            depthGauge = itemView.findViewById(R.id.comment_depth_gauge);
        }
    }

    protected GetPostResponse post = null;

    private final Markdown markdown;

    public BaseCommentFeedAdapter(View recyclerView, Connection connection, ViewModelProvider viewModelProvider) {
        super(recyclerView, connection, viewModelProvider);
        markdown = new Markdown(recyclerView.getContext());
    }

    private void updatePost(PostView post) {
        assert this.post != null && site != null && hasHeader();
        // Set Post
        PostView oldPost = this.post.post_view;
        this.post.post_view = post;
        // Reload Header
        reloadHeader();
        // Reload Comments If Post Locked/Unlock
        boolean lockChanged = !post.post.locked.equals(oldPost.post.locked);
        if (lockChanged) {
            notifier.change(0, viewModel.dataset.size());
        }
    }

    @Override
    public View createHeader(ViewGroup parent) {
        // Inflate Layout
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.comment_header, parent, false);
    }

    @Override
    protected void bindHeader(View root) {
        super.bindHeader(root);

        // Post
        View postView = root.findViewById(R.id.comments_post);
        if (post != null && site != null) {
            postView.setVisibility(View.VISIBLE);
            BasePostFeedAdapter.PostViewHolder postViewHolder = new BasePostFeedAdapter.PostViewHolder(postView);
            PostContext postContext = new PostContext() {
                @Override
                public Connection getConnection() {
                    return connection;
                }

                @Override
                public GetSiteResponse getSite() {
                    return site;
                }

                @Override
                public boolean showCreator() {
                    return true;
                }

                @Override
                public boolean showCommunity() {
                    return true;
                }

                @Override
                public PinMode getPinMode() {
                    return PinMode.INSTANCE_OR_COMMUNITY;
                }

                @Override
                public void replace(PostView oldElement, PostView newElement) {
                    assert post.post_view == oldElement;
                    updatePost(newElement);
                }

                @Override
                public boolean showText() {
                    return true;
                }

                @Override
                public Markdown getMarkdown() {
                    return markdown;
                }
            };
            BasePostFeedAdapter.bindPost(post.post_view, postViewHolder, postContext);
        } else {
            postView.setVisibility(View.GONE);
        }
    }

    @Override
    public RecyclerView.ViewHolder createItem(ViewGroup parent) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View root = inflater.inflate(R.layout.comment, parent, false);
        return new CommentViewHolder(root);
    }

    protected abstract boolean showCreator();
    protected abstract boolean showCommunity();

    protected void click(Context context, CommentView obj) {
        Intent intent = new Intent(context, CommentFeedActivity.class);
        intent.putExtra(CommentFeedActivity.COMMENT_ID_EXTRA, obj.comment.id);
        context.startActivity(intent);
    }

    protected boolean isRead(CommentView obj) {
        return true;
    }

    @Override
    protected void bindElement(@NonNull RecyclerView.ViewHolder holder, int position) {
        CommentView obj = viewModel.dataset.get(position);
        CommentViewHolder commentViewHolder = (CommentViewHolder) holder;

        // Visibility/Collapsed
        boolean visible = isVisible(obj);
        commentViewHolder.depthGauge.setVisibility(visible ? View.VISIBLE : View.GONE);
        boolean collapsed = isCollapsed(obj);

        // Card
        commentViewHolder.card.setOnClickListener(v -> click(v.getContext(), obj));

        // Text
        markdown.set(commentViewHolder.text, (!collapsed && visible) ? obj.comment.content.trim() : "");
        commentViewHolder.text.setVisibility(collapsed ? View.GONE : View.VISIBLE);

        // Karma
        int myVote = obj.my_vote != null ? obj.my_vote : 0;
        int score;
        if (site.my_user != null && !site.my_user.local_user_view.local_user.show_scores) {
            score = myVote;
        } else {
            score = obj.counts.score;
        }
        commentViewHolder.karma.setup(score, myVote, connection.hasToken(), site.site_view.local_site.enable_downvotes, (newScore, errorCallback) -> {
            CreateCommentLike method = new CreateCommentLike();
            method.comment_id = obj.comment.id;
            method.score = newScore;
            connection.send(method, commentResponse -> viewModel.dataset.replace(notifier, obj, commentResponse.comment_view), errorCallback);
        });
        ((View) commentViewHolder.karma.getParent()).setVisibility(collapsed ? View.GONE : View.VISIBLE);

        // Comment Metadata
        boolean showAvatars = true;
        if (site.my_user != null) {
            showAvatars = site.my_user.local_user_view.local_user.show_avatars;
        }
        if (!visible) {
            showAvatars = false;
        }
        boolean isEdited = obj.comment.updated != null;
        boolean blurNsfw = Images.shouldBlurNsfw(site);
        commentViewHolder.header.metadata.setup(showCreator() ? obj.creator : null, showCommunity() ? obj.community : null, isEdited ? obj.comment.updated : obj.comment.published, isEdited, blurNsfw, showAvatars, !collapsed);

        // Overflow
        commentViewHolder.header.icons.overflow.setOnClickListener(v -> new CommentOverflow(v, connection, obj) {
            @Override
            protected Integer getCurrentUser() {
                return site.my_user != null ? site.my_user.local_user_view.person.id : null;
            }

            @Override
            protected void update(CommentView newObj) {
                viewModel.dataset.replace(notifier, obj, newObj);
            }

            @Override
            protected Permissions getPermissions() {
                return permissions;
            }
        });

        // Icons
        commentViewHolder.header.icons.setup(obj.comment.deleted || obj.comment.removed, false, false, false, obj.comment.distinguished, !isRead(obj));
    }

    @Override
    public void handleEdit(Object element) {
        super.handleEdit(element);

        // Check
        if (!arePrerequisitesLoaded()) {
            return;
        }

        // Check Object
        if (element instanceof PostView) {
            // Post Header
            PostView newPost = (PostView) element;
            if (post != null && post.post_view.post.id.equals(newPost.post.id)) {
                updatePost(newPost);
            }
        } else if (element instanceof CommentView) {
            // Comment
            CommentView newComment = (CommentView) element;

            // Get Existing
            CommentView existingComment = null;
            for (CommentView comment : viewModel.dataset) {
                if (comment.comment.id.equals(newComment.comment.id)) {
                    existingComment = comment;
                    break;
                }
            }

            // Update Dataset
            if (existingComment == null) {
                // Add New Comment
                handleEditAdd(newComment);
            } else {
                // Replace Existing Comment
                viewModel.dataset.replace(notifier, existingComment, newComment);
            }
        }
    }

    protected void handleEditAdd(CommentView newComment) {
        viewModel.dataset.add(notifier, Collections.singletonList(newComment), true);
    }

    protected boolean isVisible(CommentView comment) {
        return true;
    }
    protected boolean isCollapsed(CommentView comment) {
        return false;
    }
}
