package com.thebrokenrail.combustible.activity.feed.post;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
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
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.thebrokenrail.combustible.R;
import com.thebrokenrail.combustible.activity.feed.FeedAdapter;
import com.thebrokenrail.combustible.activity.feed.comment.CommentFeedActivity;
import com.thebrokenrail.combustible.api.Connection;
import com.thebrokenrail.combustible.api.method.CreatePostLike;
import com.thebrokenrail.combustible.api.method.GetPosts;
import com.thebrokenrail.combustible.api.method.ListingType;
import com.thebrokenrail.combustible.api.method.PostView;
import com.thebrokenrail.combustible.api.method.SortType;
import com.thebrokenrail.combustible.util.CommonIcons;
import com.thebrokenrail.combustible.util.Karma;
import com.thebrokenrail.combustible.util.LinkWithIcon;
import com.thebrokenrail.combustible.util.Util;

import org.w3c.dom.Text;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

class PostFeedAdapter extends FeedAdapter<PostView> {
    private static class PostViewHolder extends RecyclerView.ViewHolder {
        private final TextView title;
        private final View karma;
        private final ImageView thumbnail;
        private final LinkWithIcon creator;
        private final TextView creatorCommunitySpacer;
        private final LinkWithIcon community;
        private final ImageView overflow;
        private final CommonIcons icons;
        private final TextView commentCount;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.post_title);
            karma = itemView.findViewById(R.id.post_karma);
            thumbnail = itemView.findViewById(R.id.post_thumbnail);
            creator = itemView.findViewById(R.id.post_creator);
            creatorCommunitySpacer = itemView.findViewById(R.id.post_creator_community_spacer);
            community = itemView.findViewById(R.id.post_community);
            overflow = itemView.findViewById(R.id.post_overflow);
            icons = new CommonIcons(itemView.findViewById(R.id.post_icons));
            commentCount = itemView.findViewById(R.id.post_comment_count);
        }
    }

    private SortType sortBy = SortType.Active;
    private ListingType listingType = ListingType.Local;

    private final boolean hasCommunityID;
    private final int communityID;

    private String bannerUrl = null;

    public PostFeedAdapter(RecyclerView parent, Connection connection, boolean hasCommunityID, int communityID) {
        super(parent, connection);
        this.hasCommunityID = hasCommunityID;
        this.communityID = communityID;
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
                sortBy = SortType.values()[position];
                refresh(true, () -> {});
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Setup Listing Type Spinner
        if (!hasCommunityID) {
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
                    listingType = ListingType.values()[position];
                    refresh(true, () -> {});
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

    void setBannerUrl(String bannerUrl) {
        if (!Objects.equals(this.bannerUrl, bannerUrl)) {
            this.bannerUrl = bannerUrl;
            notifyItemChanged(0);
        }
    }

    @Override
    protected void bindHeader(View root) {
        // Banner
        ImageView banner = root.findViewById(R.id.posts_banner);
        if (bannerUrl != null) {
            Glide.with(root.getContext())
                    .load(bannerUrl)
                    .into(banner);
            banner.setVisibility(View.VISIBLE);
        } else {
            banner.setVisibility(View.GONE);
            Glide.with(root.getContext()).clear(banner);
        }

        // Sort Spinner
        Spinner spinner = root.findViewById(R.id.posts_sort_by_spinner);
        spinner.setSelection(sortBy.ordinal());

        // Listing Type Spinner
        spinner = root.findViewById(R.id.posts_listing_type_spinner);
        spinner.setSelection(listingType.ordinal());
    }

    @Override
    public RecyclerView.ViewHolder createItem(ViewGroup parent) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View root = inflater.inflate(R.layout.post, parent, false);
        return new PostViewHolder(root);
    }

    @Override
    protected void bindElement(@NonNull RecyclerView.ViewHolder holder, int position) {
        PostView obj = dataset.get(position);
        PostViewHolder postViewHolder = (PostViewHolder) holder;

        // Title
        postViewHolder.title.setText(obj.post.name);

        // Karma
        new Karma(postViewHolder.karma, obj.counts.score, obj.my_vote != null ? obj.my_vote : 0, connection.hasToken(), new Karma.VoteCallback() {
            @Override
            public void vote(int score, Runnable successCallback, Runnable errorCallback) {
                CreatePostLike method = new CreatePostLike();
                method.post_id = obj.post.id;
                method.score = score;
                connection.send(method, postResponse -> successCallback.run(), errorCallback);
            }

            @Override
            public void storeVote(int score) {
                obj.my_vote = score;
            }
        });

        // Thumbnail
        if (obj.post.thumbnail_url != null) {
            Glide.with(postViewHolder.itemView.getContext())
                    .load(obj.post.thumbnail_url)
                    .centerCrop()
                    .placeholder(R.drawable.placeholder)
                    .into(postViewHolder.thumbnail);
            postViewHolder.thumbnail.setVisibility(View.VISIBLE);
        } else {
            postViewHolder.thumbnail.setVisibility(View.GONE);
            Glide.with(postViewHolder.itemView.getContext()).clear(postViewHolder.thumbnail);
        }

        // Post Creator
        postViewHolder.creator.setup(obj.creator.avatar, Util.getPersonName(obj.creator), () -> {
            // TODO
        });

        // Post Community
        if (!hasCommunityID) {
            postViewHolder.creatorCommunitySpacer.setVisibility(View.VISIBLE);
            postViewHolder.community.setup(obj.community.icon, Util.getCommunityName(obj.community), () -> {
                Context context = postViewHolder.itemView.getContext();
                Intent intent = new Intent(context, PostFeedActivity.class);
                intent.putExtra(PostFeedActivity.COMMUNITY_ID_EXTRA, obj.community.id);
                context.startActivity(intent);
            });
            postViewHolder.community.setVisibility(View.VISIBLE);
        } else {
            postViewHolder.creatorCommunitySpacer.setVisibility(View.GONE);
            postViewHolder.community.setup(null, "", () -> {});
            postViewHolder.community.setVisibility(View.GONE);
        }

        // Overflow
        postViewHolder.overflow.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), v);
            popup.inflate(R.menu.post_overflow);
            popup.setOnMenuItemClickListener(new PostOverflow(v.getContext(), obj));
            popup.setForceShowIcon(true);
            popup.show();
        });

        // Icons
        postViewHolder.icons.setup(obj.post.deleted || obj.post.removed, obj.post.locked, hasCommunityID ? obj.post.featured_community : obj.post.featured_local);

        // Comment Count
        postViewHolder.commentCount.setText(String.valueOf(obj.counts.comments));

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
        if (!hasCommunityID) {
            method.type_ = listingType;
        } else {
            method.community_id = communityID;
        }
        connection.send(method, getPostsResponse -> successCallback.accept(getPostsResponse.posts), errorCallback);
    }
}
