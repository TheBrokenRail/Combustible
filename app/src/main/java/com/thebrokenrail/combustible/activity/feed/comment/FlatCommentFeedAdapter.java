package com.thebrokenrail.combustible.activity.feed.comment;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.ArrayRes;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.thebrokenrail.combustible.R;
import com.thebrokenrail.combustible.activity.feed.FeedAdapter;
import com.thebrokenrail.combustible.activity.feed.post.PostFeedAdapter;
import com.thebrokenrail.combustible.activity.feed.prerequisite.FeedPrerequisite;
import com.thebrokenrail.combustible.activity.feed.prerequisite.FeedPrerequisites;
import com.thebrokenrail.combustible.api.Connection;
import com.thebrokenrail.combustible.api.method.CommentSortType;
import com.thebrokenrail.combustible.api.method.CommentView;
import com.thebrokenrail.combustible.api.method.CreateCommentLike;
import com.thebrokenrail.combustible.api.method.GetComments;
import com.thebrokenrail.combustible.api.method.GetPostResponse;
import com.thebrokenrail.combustible.api.method.GetSiteResponse;
import com.thebrokenrail.combustible.api.method.ListingType;
import com.thebrokenrail.combustible.api.method.PostView;
import com.thebrokenrail.combustible.util.markdown.Markdown;
import com.thebrokenrail.combustible.widget.CommonIcons;
import com.thebrokenrail.combustible.widget.Karma;
import com.thebrokenrail.combustible.util.Util;
import com.thebrokenrail.combustible.widget.Metadata;

import java.util.List;
import java.util.function.Consumer;

public class FlatCommentFeedAdapter extends FeedAdapter<CommentView> {
    protected static class CommentViewHolder extends RecyclerView.ViewHolder {
        protected final CardView card;
        private final TextView text;
        private final Karma karma;
        private final Metadata metadata;
        private final CommonIcons icons;
        protected final Button showMore;
        private final Button viewAll;

        private CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.comment_card);
            text = itemView.findViewById(R.id.comment_text);
            karma = itemView.findViewById(R.id.comment_karma);
            metadata = itemView.findViewById(R.id.comment_metadata);
            icons = itemView.findViewById(R.id.comment_icons);
            showMore = itemView.findViewById(R.id.comment_show_more);
            viewAll = itemView.findViewById(R.id.comment_view_all);
        }
    }

    protected interface SortingMethod {
        Enum<?> get(int position);
        @ArrayRes int values();
    }
    protected SortingMethod sortingMethod = new SortingMethod() {
        @Override
        public Enum<?> get(int position) {
            return CommentSortType.values()[position];
        }

        @Override
        public int values() {
            return R.array.comment_sort_types;
        }
    };
    protected Enum<?> sortBy = CommentSortType.Hot;

    public enum ParentType {
        POST,
        COMMENT
    }
    protected final ParentType parentType;
    protected final int parent;

    private PostView post = null;

    private final Markdown markdown;

    public FlatCommentFeedAdapter(View recyclerView, Connection connection, ViewModelProvider viewModelProvider, String viewModelKey, ParentType parentType, int parent) {
        super(recyclerView, connection, viewModelProvider, viewModelKey);
        this.parentType = parentType;
        this.parent = parent;
        if (recyclerView != null) {
            markdown = new Markdown(recyclerView.getContext());
        } else {
            markdown = null;
        }
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
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(parent.getContext(), sortingMethod.values(), textViewResId);
        int dropDownViewResource = android.R.layout.simple_spinner_dropdown_item;
        adapter.setDropDownViewResource(dropDownViewResource);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Enum<?> newSortBy = sortingMethod.get(position);
                if (sortBy != newSortBy) {
                    sortBy = newSortBy;
                    refresh(true, () -> {});
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Return
        return root;
    }

    @Override
    protected void handlePrerequisites(FeedPrerequisites prerequisites) {
        super.handlePrerequisites(prerequisites);
        if (parentType == ParentType.POST) {
            prerequisites.require(FeedPrerequisite.Post.class);
        }
        prerequisites.listen(prerequisite -> {
            if (prerequisite instanceof FeedPrerequisite.Site) {
                // Reload Header
                notifyItemChanged(0);
            } else if (parentType == ParentType.POST && prerequisite instanceof FeedPrerequisite.Post) {
                GetPostResponse getPostResponse = ((FeedPrerequisite.Post) prerequisite).get();
                post = getPostResponse.post_view;
                // Wait Until Site Has Loaded
                if (site != null) {
                    // Reload Header
                    notifyItemChanged(0);
                }
            }
        });
    }

    @Override
    protected void bindHeader(View root) {
        // Sort Spinner
        Spinner spinner = root.findViewById(R.id.comments_sort_by_spinner);
        spinner.setSelection(sortBy.ordinal());
        spinner.setEnabled(arePrerequisitesLoaded());

        // Post
        View postView = root.findViewById(R.id.comments_post);
        if (post != null && site != null) {
            postView.setVisibility(View.VISIBLE);
            PostFeedAdapter.PostViewHolder postViewHolder = new PostFeedAdapter.PostViewHolder(postView);
            PostFeedAdapter.PostContext postContext = new PostFeedAdapter.PostContext() {
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
                    return false;
                }

                @Override
                public PostFeedAdapter.PinMode getPinMode() {
                    return PostFeedAdapter.PinMode.COMMUNITY;
                }

                @Override
                public void replace(PostView oldElement, PostView newElement) {
                    assert post == oldElement;
                    post = newElement;
                    notifyItemChanged(0);
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
            PostFeedAdapter.bindPost(post, postViewHolder, postContext);
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

    protected boolean showCreator() {
        return true;
    }

    protected boolean showCommunity() {
        return false;
    }

    @Override
    protected void bindElement(@NonNull RecyclerView.ViewHolder holder, int position) {
        CommentView obj = viewModel.dataset.get(position);
        CommentViewHolder commentViewHolder = (CommentViewHolder) holder;

        // Card
        commentViewHolder.card.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, CommentFeedActivity.class);
            intent.putExtra(CommentFeedActivity.COMMENT_ID_EXTRA, obj.comment.id);
            context.startActivity(intent);
        });

        // Text
        markdown.set(commentViewHolder.text, obj.comment.content.trim());

        // Karma
        commentViewHolder.karma.setup(obj.counts.score, obj.my_vote != null ? obj.my_vote : 0, connection.hasToken(), site.site_view.local_site.enable_downvotes, (score, errorCallback) -> {
            CreateCommentLike method = new CreateCommentLike();
            method.comment_id = obj.comment.id;
            method.score = score;
            connection.send(method, commentResponse -> replace(obj, commentResponse.comment_view), errorCallback);
        });

        // Comment Metadata
        boolean isEdited = obj.comment.updated != null;
        boolean blurNsfw = true;
        if (site.my_user != null) {
            blurNsfw = site.my_user.local_user_view.local_user.blur_nsfw;
        }
        commentViewHolder.metadata.setup(showCreator() ? obj.creator : null, showCommunity() ? obj.community : null, isEdited ? obj.comment.updated : obj.comment.published, isEdited, blurNsfw);

        // Overflow
        commentViewHolder.icons.overflow.setOnClickListener(v -> new CommentOverflow(v, connection, obj, newObj -> replace(obj, newObj)));

        // Icons
        commentViewHolder.icons.setup(obj.comment.deleted || obj.comment.removed, false, false, obj.comment.distinguished);

        // View All
        if (parentType == ParentType.COMMENT && obj.comment.id == parent) {
            commentViewHolder.viewAll.setVisibility(View.VISIBLE);
            commentViewHolder.viewAll.setOnClickListener(v -> {
                Context context = v.getContext();
                Intent intent = new Intent(context, CommentFeedActivity.class);
                intent.putExtra(CommentFeedActivity.POST_ID_EXTRA, obj.post.id);
                context.startActivity(intent);
            });
        } else {
            commentViewHolder.viewAll.setVisibility(View.GONE);
        }
    }

    @Override
    protected void loadPage(int page, Consumer<List<CommentView>> successCallback, Runnable errorCallback) {
        GetComments method = new GetComments();
        method.page = page;
        method.limit = Util.ELEMENTS_PER_PAGE;
        method.sort = (CommentSortType) sortBy;
        if (parentType == ParentType.POST) {
            method.post_id = parent;
        } else {
            method.parent_id = parent;
        }
        method.type_ = ListingType.All;
        connection.send(method, getCommentsResponse -> successCallback.accept(getCommentsResponse.comments), errorCallback);
    }
}
