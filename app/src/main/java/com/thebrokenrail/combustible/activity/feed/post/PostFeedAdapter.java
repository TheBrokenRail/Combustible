package com.thebrokenrail.combustible.activity.feed.post;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.thebrokenrail.combustible.R;
import com.thebrokenrail.combustible.activity.ViewImageActivity;
import com.thebrokenrail.combustible.activity.feed.FeedAdapter;
import com.thebrokenrail.combustible.activity.feed.comment.CommentFeedActivity;
import com.thebrokenrail.combustible.activity.feed.prerequisite.FeedPrerequisite;
import com.thebrokenrail.combustible.activity.feed.prerequisite.FeedPrerequisites;
import com.thebrokenrail.combustible.api.Connection;
import com.thebrokenrail.combustible.api.method.CreatePostLike;
import com.thebrokenrail.combustible.api.method.GetCommunityResponse;
import com.thebrokenrail.combustible.api.method.GetPosts;
import com.thebrokenrail.combustible.api.method.GetSiteResponse;
import com.thebrokenrail.combustible.api.method.ListingType;
import com.thebrokenrail.combustible.api.method.PostView;
import com.thebrokenrail.combustible.api.method.SortType;
import com.thebrokenrail.combustible.util.DrawableAlwaysCrossFadeFactory;
import com.thebrokenrail.combustible.util.Util;
import com.thebrokenrail.combustible.util.markdown.Markdown;
import com.thebrokenrail.combustible.widget.CommonIcons;
import com.thebrokenrail.combustible.widget.Karma;
import com.thebrokenrail.combustible.widget.Metadata;
import com.thebrokenrail.combustible.widget.SquareView;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class PostFeedAdapter extends FeedAdapter<PostView> {
    public static class PostViewHolder extends RecyclerView.ViewHolder {
        private final TextView title;
        private final Karma karma;
        private final AppCompatImageView thumbnail;
        private final AppCompatImageView bigThumbnail;
        private final int cornerRadius;
        private final Drawable placeholder;
        private final TextView text;
        private final Metadata metadata;
        private final CommonIcons icons;
        private final TextView commentCount;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.post_title);
            karma = itemView.findViewById(R.id.post_karma);
            thumbnail = itemView.findViewById(R.id.post_thumbnail);
            bigThumbnail = itemView.findViewById(R.id.post_big_thumbnail);
            text = itemView.findViewById(R.id.post_text);
            metadata = itemView.findViewById(R.id.post_metadata);
            icons = itemView.findViewById(R.id.post_icons);
            commentCount = itemView.findViewById(R.id.post_comment_count);
            // Setup Thumbnails
            thumbnail.setBackgroundDrawable(Util.createThumbnailBackground(itemView.getContext()));
            bigThumbnail.setBackgroundDrawable(Util.createThumbnailBackground(itemView.getContext()));
            ((SquareView) bigThumbnail.getParent()).setForeground(Util.createThumbnailForeground(itemView.getContext()));
            cornerRadius = Util.getCornerRadius(itemView.getContext());
            placeholder = Util.createPlaceholder(itemView.getContext());
        }
    }

    protected SortType sortBy = SortType.Active;
    private ListingType listingType = ListingType.Local;

    private final boolean hasCommunityID;
    private final int communityID;

    private final Markdown markdown;

    private String bannerUrl = null;
    private boolean bannerNsfw = false;

    public PostFeedAdapter(View parent, Connection connection, ViewModelProvider viewModelProvider, String viewModelKey, boolean hasCommunityID, int communityID) {
        super(parent, connection, viewModelProvider, viewModelKey);
        this.hasCommunityID = hasCommunityID;
        this.communityID = communityID;
        markdown = new Markdown(parent.getContext());
    }

    @Override
    public boolean hasHeader() {
        return true;
    }

    protected boolean hasListingTypeSort() {
        return !hasCommunityID;
    }

    @Override
    public View createHeader(ViewGroup parent) {
        // Inflate Layout
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View root = inflater.inflate(R.layout.post_header, parent, false);

        // Setup Sort Spinner
        Spinner spinner = root.findViewById(R.id.posts_sort_by_spinner);
        int textViewResId = android.R.layout.simple_spinner_item;
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(parent.getContext(), R.array.sort_types, textViewResId);
        int dropDownViewResource = android.R.layout.simple_spinner_dropdown_item;
        adapter.setDropDownViewResource(dropDownViewResource);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SortType newSortBy = SortType.values()[position];
                if (sortBy != newSortBy) {
                    sortBy = newSortBy;
                    refresh(true, () -> {});
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Setup Listing Type Spinner
        if (hasListingTypeSort()) {
            spinner = root.findViewById(R.id.posts_listing_type_spinner);
            CharSequence[] listingTypes = context.getResources().getTextArray(R.array.listing_types);
            adapter = new ArrayAdapter<CharSequence>(context, textViewResId, 0, Arrays.asList(listingTypes)) {
                @Override
                public boolean isEnabled(int position) {
                    if (position == ListingType.Subscribed.ordinal()) {
                        return connection.hasToken();
                    } else {
                        return super.isEnabled(position);
                    }
                }

                @Override
                public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                    View view = super.getDropDownView(position, convertView, parent);
                    view.setEnabled(isEnabled(position));
                    return view;
                }
            };
            adapter.setDropDownViewResource(dropDownViewResource);
            spinner.setAdapter(adapter);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    ListingType newListingType = ListingType.values()[position];
                    if (listingType != newListingType) {
                        listingType = newListingType;
                        refresh(true, () -> {});
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        } else {
            root.findViewById(R.id.posts_listing_type).setVisibility(View.GONE);
        }

        // Return
        return root;
    }

    @Override
    protected void handlePrerequisites(FeedPrerequisites prerequisites) {
        super.handlePrerequisites(prerequisites);
        if (showBanner() && hasCommunityID) {
            prerequisites.require(FeedPrerequisite.Community.class);
        }
        prerequisites.listen(prerequisite -> {
            boolean reloadHeader = false;
            if (prerequisite instanceof FeedPrerequisite.Site) {
                // Site Loaded
                assert site != null;
                if (useDefaultSort() && site.my_user != null) {
                    // Custom Sorting Defaults
                    sortBy = site.my_user.local_user_view.local_user.default_sort_type;
                    if (!hasCommunityID) {
                        listingType = site.my_user.local_user_view.local_user.default_listing_type;
                    }
                }
                reloadHeader = true;

                // Banner
                if (showBanner() && !hasCommunityID) {
                    setBannerUrl(site.site_view.site.banner, false);
                }
            } else if (showBanner() && hasCommunityID && prerequisite instanceof FeedPrerequisite.Community) {
                // Banner
                GetCommunityResponse getCommunityResponse = ((FeedPrerequisite.Community) prerequisite).get();
                reloadHeader = setBannerUrl(getCommunityResponse.community_view.community.banner, getCommunityResponse.community_view.community.nsfw);
            }
            if (reloadHeader) {
                // Reload Header
                notifyItemChanged(0);
            }
        });
    }

    protected boolean showBanner() {
        return true;
    }

    protected boolean useDefaultSort() {
        return true;
    }

    private boolean setBannerUrl(String bannerUrl, boolean bannerNsfw) {
        if (!Objects.equals(this.bannerUrl, bannerUrl)) {
            this.bannerUrl = bannerUrl;
            this.bannerNsfw = bannerNsfw;
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void bindHeader(View root) {
        // Banner
        boolean blurNsfw = true;
        if (site != null && site.my_user != null) {
            blurNsfw = site.my_user.local_user_view.local_user.blur_nsfw;
        }
        boolean blurBanner = bannerNsfw && blurNsfw;
        ImageView banner = root.findViewById(R.id.posts_banner);
        if (bannerUrl != null) {
            Glide.with(root.getContext())
                    .load(bannerUrl)
                    .transform(Util.addBlurTransformation(blurBanner))
                    .into(banner);
            banner.setVisibility(View.VISIBLE);
        } else {
            banner.setVisibility(View.GONE);
            Glide.with(root.getContext()).clear(banner);
        }

        // Sort Spinner
        Spinner spinner = root.findViewById(R.id.posts_sort_by_spinner);
        spinner.setSelection(sortBy.ordinal());
        spinner.setEnabled(arePrerequisitesLoaded());

        // Listing Type Spinner
        spinner = root.findViewById(R.id.posts_listing_type_spinner);
        spinner.setSelection(listingType.ordinal());
        spinner.setEnabled(arePrerequisitesLoaded());
    }

    @Override
    public RecyclerView.ViewHolder createItem(ViewGroup parent) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View root = inflater.inflate(R.layout.post, parent, false);
        return new PostViewHolder(root);
    }

    protected boolean showCreator() {
        return true;
    }

    protected boolean showCommunity() {
        return !hasCommunityID;
    }

    public enum PinMode {
        HOME,
        COMMUNITY,
        NONE
    }

    protected PinMode getPinMode() {
        if (hasCommunityID) {
            return PinMode.COMMUNITY;
        } else {
            return PinMode.HOME;
        }
    }

    public interface PostContext {
        Connection getConnection();
        GetSiteResponse getSite();
        boolean showCreator();
        boolean showCommunity();
        PinMode getPinMode();
        void replace(PostView oldElement, PostView newElement);
        boolean showText();
        Markdown getMarkdown();
    }

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
            return PostFeedAdapter.this.showCreator();
        }

        @Override
        public boolean showCommunity() {
            return PostFeedAdapter.this.showCommunity();
        }

        @Override
        public PinMode getPinMode() {
            return PostFeedAdapter.this.getPinMode();
        }

        @Override
        public void replace(PostView oldElement, PostView newElement) {
            PostFeedAdapter.this.replace(oldElement, newElement);
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
        postViewHolder.karma.setup(obj.counts.score, obj.my_vote != null ? obj.my_vote : 0, connection.hasToken(), site.site_view.local_site.enable_downvotes, (score, errorCallback) -> {
            CreatePostLike method = new CreatePostLike();
            method.post_id = obj.post.id;
            method.score = score;
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
        boolean useBigThumbnail = false;
        if (Util.isImage(obj.post.url)) {
            thumbnailUrl = obj.post.url;
            useBigThumbnail = true;
        }

        // Thumbnail
        postViewHolder.thumbnail.setVisibility(View.GONE);
        Glide.with(postViewHolder.itemView.getContext()).clear(postViewHolder.thumbnail);
        SquareView bigThumbnailParent = (SquareView) postViewHolder.bigThumbnail.getParent();
        bigThumbnailParent.setVisibility(View.GONE);
        Glide.with(postViewHolder.itemView.getContext()).clear(postViewHolder.bigThumbnail);
        if (thumbnailUrl != null) {
            AppCompatImageView thumbnail;
            if (useBigThumbnail) {
                thumbnail = postViewHolder.bigThumbnail;
                bigThumbnailParent.setVisibility(View.VISIBLE);
            } else {
                thumbnail = postViewHolder.thumbnail;
                thumbnail.setVisibility(View.VISIBLE);
            }
            thumbnail.setAdjustViewBounds(false);
            Glide.with(postViewHolder.itemView.getContext())
                    .load(thumbnailUrl)
                    .transition(DrawableTransitionOptions.with(new DrawableAlwaysCrossFadeFactory()))
                    .transform(Util.addBlurTransformation(blurThumbnail, new CenterCrop(), new RoundedCorners(postViewHolder.cornerRadius)))
                    .placeholder(postViewHolder.placeholder)
                    .into(thumbnail);
        }

        // Thumbnail Click Handler
        bigThumbnailParent.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, ViewImageActivity.class);
            intent.putExtra(ViewImageActivity.IMAGE_URL_EXTRA, obj.post.url);
            context.startActivity(intent);
        });

        // Post Metadata
        boolean isEdited = obj.post.updated != null;
        postViewHolder.metadata.setup(postContext.showCreator() ? obj.creator : null, postContext.showCommunity() ? obj.community : null, isEdited ? obj.post.updated : obj.post.published, isEdited, blurNsfw);

        // Overflow
        postViewHolder.icons.overflow.setOnClickListener(v -> new PostOverflow(v, connection, obj, newObj -> postContext.replace(obj, newObj)) {
            @Override
            protected boolean showShare() {
                return !postContext.showText();
            }
        });

        // Pinned Post
        boolean isPinned;
        PinMode pinMode = postContext.getPinMode();
        if (pinMode == PinMode.HOME) {
            isPinned = obj.post.featured_local;
        } else if (pinMode == PinMode.COMMUNITY) {
            isPinned = obj.post.featured_community;
        } else {
            isPinned = false;
        }

        // Icons
        boolean isDeleted = obj.post.deleted || obj.post.removed;
        boolean isLocked = obj.post.locked;
        postViewHolder.icons.setup(isDeleted, isNsfw, isLocked, isPinned);

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
    protected void loadPage(int page, Consumer<List<PostView>> successCallback, Runnable errorCallback) {
        GetPosts method = new GetPosts();
        method.page = page;
        method.limit = Util.ELEMENTS_PER_PAGE;
        method.sort = sortBy;
        if (hasListingTypeSort()) {
            method.type_ = listingType;
        } else {
            method.type_ = ListingType.All;
            method.community_id = communityID;
        }
        connection.send(method, getPostsResponse -> successCallback.accept(getPostsResponse.posts), errorCallback);
    }
}
