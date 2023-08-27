package com.thebrokenrail.combustible.activity.feed.post;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.thebrokenrail.combustible.R;
import com.thebrokenrail.combustible.activity.ViewImageActivity;
import com.thebrokenrail.combustible.activity.feed.SortableFeedAdapter;
import com.thebrokenrail.combustible.activity.feed.comment.CommentFeedActivity;
import com.thebrokenrail.combustible.api.Connection;
import com.thebrokenrail.combustible.api.method.CreatePostLike;
import com.thebrokenrail.combustible.api.method.GetSiteResponse;
import com.thebrokenrail.combustible.api.method.PostView;
import com.thebrokenrail.combustible.util.Images;
import com.thebrokenrail.combustible.util.Links;
import com.thebrokenrail.combustible.util.Permissions;
import com.thebrokenrail.combustible.util.glide.GlideApp;
import com.thebrokenrail.combustible.util.glide.GlideUtil;
import com.thebrokenrail.combustible.util.markdown.Markdown;
import com.thebrokenrail.combustible.widget.CommonIcons;
import com.thebrokenrail.combustible.widget.Karma;
import com.thebrokenrail.combustible.widget.Metadata;

import okhttp3.HttpUrl;

public abstract class BasePostFeedAdapter extends SortableFeedAdapter<PostView> {
    public static class PostViewHolder extends RecyclerView.ViewHolder {
        private static final double ONE_MINUS_SIN_45 = 1.0 - (1.0 / Math.sqrt(2.0));

        private final AppCompatTextView title;
        private final Karma karma;
        private final AppCompatImageView thumbnail;
        private final AppCompatTextView thumbnailHint;
        private final AppCompatImageView bigThumbnail;
        private final int cornerRadius;
        private final Drawable placeholder;
        private final AppCompatTextView text;
        private final Metadata metadata;
        private final CommonIcons icons;
        private final AppCompatTextView commentCount;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.post_title);
            karma = itemView.findViewById(R.id.post_karma);
            thumbnail = itemView.findViewById(R.id.post_thumbnail);
            thumbnailHint = itemView.findViewById(R.id.post_thumbnail_hint);
            bigThumbnail = itemView.findViewById(R.id.post_big_thumbnail);
            text = itemView.findViewById(R.id.post_text);
            metadata = itemView.findViewById(R.id.post_metadata);
            icons = itemView.findViewById(R.id.post_icons);
            commentCount = itemView.findViewById(R.id.post_comment_count);

            // Setup Thumbnails
            thumbnail.setBackgroundDrawable(Images.createThumbnailBackground(itemView.getContext()));
            ((FrameLayout) thumbnail.getParent()).setForeground(Images.createThumbnailForeground(itemView.getContext()));
            thumbnailHint.setBackgroundDrawable(Images.createThumbnailHintBackground(itemView.getContext()));
            cornerRadius = Images.getCornerRadius(itemView.getContext());
            int cornerPadding = (int) (cornerRadius * ONE_MINUS_SIN_45);
            thumbnailHint.setPadding(cornerPadding, cornerPadding, cornerPadding, cornerPadding);
            bigThumbnail.setBackgroundDrawable(Images.createThumbnailBackground(itemView.getContext()));
            ((FrameLayout) bigThumbnail.getParent()).setForeground(Images.createThumbnailForeground(itemView.getContext()));
            placeholder = Images.createPlaceholder(itemView.getContext());
        }
    }

    private final Markdown markdown;

    private String bannerUrl = null;
    private boolean bannerNsfw = false;

    public BasePostFeedAdapter(View parent, Connection connection, ViewModelProvider viewModelProvider) {
        super(parent, connection, viewModelProvider);
        markdown = new Markdown(parent.getContext());
    }

    @Override
    public View createHeader(ViewGroup parent) {
        // Inflate Layout
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.post_header, parent, false);
    }

    protected abstract boolean showBanner();

    protected void setBannerUrl(String bannerUrl, boolean bannerNsfw) {
        this.bannerUrl = bannerUrl;
        this.bannerNsfw = bannerNsfw;
    }

    @Override
    protected void bindHeader(View root) {
        super.bindHeader(root);

        // Banner
        boolean blurNsfw = true;
        if (site != null && site.my_user != null) {
            blurNsfw = site.my_user.local_user_view.local_user.blur_nsfw;
        }
        boolean blurBanner = bannerNsfw && blurNsfw;
        ImageView banner = root.findViewById(R.id.posts_banner);
        RequestManager requestManager = GlideApp.with(root.getContext());
        if (bannerUrl != null) {
            GlideUtil.load(requestManager, bannerUrl, new FitCenter(), 0, blurBanner, false, null, banner);
            banner.setVisibility(View.VISIBLE);
        } else {
            banner.setVisibility(View.GONE);
            requestManager.clear(banner);
        }
    }

    @Override
    public RecyclerView.ViewHolder createItem(ViewGroup parent) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View root = inflater.inflate(R.layout.post, parent, false);
        return new PostViewHolder(root);
    }

    protected abstract boolean showCreator();

    protected abstract boolean showCommunity();

    protected abstract PostContext.PinMode getPinMode();

    private final PostContext postContext = new PostContext() {
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
            return BasePostFeedAdapter.this.showCreator();
        }

        @Override
        public boolean showCommunity() {
            return BasePostFeedAdapter.this.showCommunity();
        }

        @Override
        public PinMode getPinMode() {
            return BasePostFeedAdapter.this.getPinMode();
        }

        @Override
        public void replace(PostView oldElement, PostView newElement) {
            viewModel.dataset.replace(notifier, oldElement, newElement);
        }

        @Override
        public boolean showText() {
            return false;
        }

        @Override
        public Markdown getMarkdown() {
            return markdown;
        }
    };

    public static void bindPost(PostView obj, PostViewHolder postViewHolder, PostContext postContext) {
        Connection connection = postContext.getConnection();
        GetSiteResponse site = postContext.getSite();

        // Title
        postViewHolder.title.setText(obj.post.name);

        // Karma
        int myVote = obj.my_vote != null ? obj.my_vote : 0;
        int score;
        if (site.my_user != null && !site.my_user.local_user_view.local_user.show_scores) {
            score = myVote;
        } else {
            score = obj.counts.score;
        }
        postViewHolder.karma.setup(score, myVote, connection.hasToken(), site.site_view.local_site.enable_downvotes, (newScore, errorCallback) -> {
            CreatePostLike method = new CreatePostLike();
            method.post_id = obj.post.id;
            method.score = newScore;
            connection.send(method, postResponse -> postContext.replace(obj, postResponse.post_view), errorCallback);
        });

        // Thumbnail Blurring
        boolean isNsfw = obj.post.nsfw || obj.community.nsfw;
        boolean blurNsfw = true;
        if (site.my_user != null) {
            blurNsfw = site.my_user.local_user_view.local_user.blur_nsfw;
        }
        boolean blurThumbnail = isNsfw && blurNsfw;

        // Pick Thumbnail Size
        String thumbnailUrl = obj.post.thumbnail_url;
        boolean showThumbnail = obj.post.url != null;
        boolean useBigThumbnail = false;
        boolean disableLargeThumbnail = PreferenceManager.getDefaultSharedPreferences(postViewHolder.itemView.getContext()).getBoolean("disable_large_thumbnail", false);
        if (Images.isImage(obj.post.url) && !disableLargeThumbnail) {
            thumbnailUrl = obj.post.url;
            useBigThumbnail = true;
        }

        // Thumbnail
        AppCompatImageView thumbnail = postViewHolder.thumbnail;
        AppCompatImageView otherThumbnail = postViewHolder.bigThumbnail;
        if (useBigThumbnail) {
            // Swap
            AppCompatImageView x = thumbnail;
            thumbnail = otherThumbnail;
            otherThumbnail = x;
        }
        FrameLayout thumbnailParent = (FrameLayout) thumbnail.getParent();
        FrameLayout otherThumbnailParent = (FrameLayout) otherThumbnail.getParent();
        RequestManager requestManager = GlideApp.with(postViewHolder.itemView.getContext());
        if (showThumbnail) {
            thumbnailParent.setVisibility(View.VISIBLE);
            thumbnail.setAdjustViewBounds(false);
            GlideUtil.load(requestManager, thumbnailUrl, new CenterCrop(), postViewHolder.cornerRadius, blurThumbnail, true, postViewHolder.placeholder, thumbnail);
        } else {
            thumbnailParent.setVisibility(View.GONE);
            requestManager.clear(thumbnail);
        }
        otherThumbnailParent.setVisibility(View.GONE);
        requestManager.clear(otherThumbnail);

        // Thumbnail Click Handler
        boolean finalUseBigThumbnail = useBigThumbnail;
        thumbnailParent.setOnClickListener(v -> {
            Context context = v.getContext();
            if (finalUseBigThumbnail) {
                Intent intent = new Intent(context, ViewImageActivity.class);
                intent.putExtra(ViewImageActivity.IMAGE_URL_EXTRA, obj.post.url);
                context.startActivity(intent);
            } else {
                Links.open(context, obj.post.url);
            }
        });

        // Thumbnail Hint
        if (showThumbnail && !useBigThumbnail) {
            HttpUrl url = HttpUrl.parse(obj.post.url);
            assert url != null;
            String host = url.host();
            String wwwPrefix = "www.";
            if (host.startsWith(wwwPrefix)) {
                host = host.substring(wwwPrefix.length());
            }
            postViewHolder.thumbnailHint.setText(host);
        }

        // Post Metadata
        boolean showAvatars = true;
        if (site.my_user != null) {
            showAvatars = site.my_user.local_user_view.local_user.show_avatars;
        }
        boolean isEdited = obj.post.updated != null;
        postViewHolder.metadata.setup(postContext.showCreator() ? obj.creator : null, postContext.showCommunity() ? obj.community : null, isEdited ? obj.post.updated : obj.post.published, isEdited, blurNsfw, showAvatars);

        // Overflow
        postViewHolder.icons.overflow.setOnClickListener(v -> new PostOverflow(v, connection, obj) {
            @Override
            protected boolean showShare() {
                return !postContext.showText();
            }

            @Override
            protected Integer getCurrentUser() {
                return site.my_user != null ? site.my_user.local_user_view.person.id : null;
            }

            @Override
            protected void update(PostView newObj) {
                postContext.replace(obj, newObj);
            }

            @Override
            protected Permissions getPermissions() {
                Permissions permissions = new Permissions();
                permissions.setSite(postContext.getSite());
                return permissions;
            }
        });

        // Pinned Post
        boolean isPinned;
        PostContext.PinMode pinMode = postContext.getPinMode();
        if (pinMode == PostContext.PinMode.HOME) {
            isPinned = obj.post.featured_local;
        } else if (pinMode == PostContext.PinMode.COMMUNITY) {
            isPinned = obj.post.featured_community;
        } else {
            isPinned = false;
        }

        // Icons
        boolean isDeleted = obj.post.deleted || obj.post.removed;
        boolean isLocked = obj.post.locked;
        postViewHolder.icons.setup(isDeleted, isNsfw, isLocked, isPinned, false, false);

        // Comment Count
        postViewHolder.commentCount.setText(String.valueOf(obj.counts.comments));

        // Build Text
        String text = obj.post.body;
        if (text == null) {
            text = "";
        }
        if (!useBigThumbnail && obj.post.url != null) {
            text = obj.post.url + "\n\n" + text;
        }
        text = text.trim();

        // Text
        if (postContext.showText() && text.length() > 0) {
            postViewHolder.text.setVisibility(View.VISIBLE);
            postContext.getMarkdown().set(postViewHolder.text, text);
        } else {
            postViewHolder.text.setVisibility(View.GONE);
        }
    }

    @Override
    protected void bindElement(@NonNull RecyclerView.ViewHolder holder, int position) {
        PostView obj = viewModel.dataset.get(position);
        PostViewHolder postViewHolder = (PostViewHolder) holder;

        // Bind
        bindPost(obj, postViewHolder, postContext);

        // On Click
        postViewHolder.itemView.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, CommentFeedActivity.class);
            intent.putExtra(CommentFeedActivity.POST_ID_EXTRA, obj.post.id);
            context.startActivity(intent);
        });
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
            // Edited Post
            PostView newPost = (PostView) element;

            // Get Existing
            PostView existingPost = null;
            for (PostView post : viewModel.dataset) {
                if (post.post.id.equals(newPost.post.id)) {
                    existingPost = post;
                    break;
                }
            }
            if (existingPost == null) {
                // Can't Find Post
                return;
            }

            // Replace
            viewModel.dataset.replace(notifier, existingPost, newPost);
        }
    }
}
