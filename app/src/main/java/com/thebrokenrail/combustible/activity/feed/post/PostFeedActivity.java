package com.thebrokenrail.combustible.activity.feed.post;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.thebrokenrail.combustible.R;
import com.thebrokenrail.combustible.activity.feed.FeedActivity;
import com.thebrokenrail.combustible.activity.feed.FeedAdapter;
import com.thebrokenrail.combustible.activity.feed.prerequisite.FeedPrerequisite;
import com.thebrokenrail.combustible.activity.feed.prerequisite.FeedPrerequisites;
import com.thebrokenrail.combustible.api.method.CommunityView;
import com.thebrokenrail.combustible.api.method.FollowCommunity;
import com.thebrokenrail.combustible.api.method.GetCommunityResponse;
import com.thebrokenrail.combustible.api.method.GetSiteResponse;
import com.thebrokenrail.combustible.api.method.SubscribedType;
import com.thebrokenrail.combustible.util.Sharing;
import com.thebrokenrail.combustible.util.Util;
import com.thebrokenrail.combustible.util.markdown.Markdown;
import com.thebrokenrail.combustible.widget.PossiblyOutlinedButton;

import java.util.Objects;

public class PostFeedActivity extends FeedActivity {
    public static final String COMMUNITY_ID_EXTRA = "com.thebrokenrail.combustible.COMMUNITY_ID_EXTRA";

    private boolean hasCommunityID = false;
    private int communityID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set Title
        ActionBar actionBar = Objects.requireNonNull(getSupportActionBar());
        if (!hasCommunityID) {
            actionBar.setTitle(R.string.posts_home);
        } else {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onCreateBeforeAdapter() {
        super.onCreateBeforeAdapter();

        // Get Community ID
        Intent intent = getIntent();
        hasCommunityID = intent.hasExtra(COMMUNITY_ID_EXTRA);
        if (hasCommunityID) {
            communityID = intent.getIntExtra(COMMUNITY_ID_EXTRA, 0);
        }
    }

    @Override
    protected FeedAdapter<?> createAdapter(RecyclerView feed) {
        return new PostFeedAdapter(feed, connection, new ViewModelProvider(this), "posts", hasCommunityID, communityID);
    }

    @Override
    protected void addPrerequisites(FeedPrerequisites prerequisites) {
        super.addPrerequisites(prerequisites);

        // Load Community
        ActionBar actionBar = Objects.requireNonNull(getSupportActionBar());
        if (hasCommunityID) {
            prerequisites.add(new FeedPrerequisite.Community(communityID));
        }
        prerequisites.listen(prerequisite -> {
            if (hasCommunityID && prerequisite instanceof FeedPrerequisite.Community) {
                GetCommunityResponse getCommunityResponse = ((FeedPrerequisite.Community) prerequisite).get();

                // Title
                actionBar.setTitle(getCommunityResponse.community_view.community.title);
                // Subtitle
                actionBar.setSubtitle('!' + Util.getCommunityName(getCommunityResponse.community_view.community));
                // Info
                setInfo(getCommunityResponse.community_view.community.description, getCommunityResponse.community_view.subscribed);
                infoCommunity = getCommunityResponse.community_view;
            } else if (!hasCommunityID && prerequisite instanceof FeedPrerequisite.Site) {
                GetSiteResponse getSiteResponse = ((FeedPrerequisite.Site) prerequisite).get();

                // Info
                setInfo(getSiteResponse.site_view.site.sidebar, null);
            }
        });
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean ret = super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.feed_show_info).setEnabled(infoAvailable);
        if (canShare()) {
            menu.findItem(R.id.feed_share).setEnabled(infoAvailable);
        }
        return ret;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (item.getItemId() == R.id.feed_show_info) {
            openInfo();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private boolean infoAvailable = false;
    private String infoText = "";
    private SubscribedType infoSubscribedType = null;

    private void setInfo(String text, SubscribedType subscribedType) {
        // Check
        if (text != null && text.trim().length() == 0) {
            text = null;
        }
        if (text == null && subscribedType == null) {
            return;
        }
        // Enable Info Button
        infoAvailable = true;
        invalidateOptionsMenu();
        // Store
        infoText = text;
        infoSubscribedType = subscribedType;
    }

    private void updateSubscriptionText(PossiblyOutlinedButton button) {
        button.setText(getResources().getStringArray(R.array.subscribed_type)[infoSubscribedType.ordinal()]);
        button.setOutlined(infoSubscribedType != SubscribedType.NotSubscribed);
    }

    private void openInfo() {
        // Create Layout
        @SuppressLint("InflateParams") View root = getLayoutInflater().inflate(R.layout.dialog_community_info, null);
        PossiblyOutlinedButton subscribeButton = root.findViewById(R.id.community_subscribe);
        TextView infoView = root.findViewById(R.id.community_info);

        // Subscribe
        subscribeButton.setEnabled(connection.hasToken());
        if (infoSubscribedType != null) {
            subscribeButton.setButtonOnClickListener(v -> {
                FollowCommunity method = new FollowCommunity();
                method.follow = infoSubscribedType == SubscribedType.NotSubscribed;
                method.community_id = communityID;
                connection.send(method, communityResponse -> {
                    infoSubscribedType = communityResponse.community_view.subscribed;
                    updateSubscriptionText(subscribeButton);
                }, () -> Util.unknownError(PostFeedActivity.this));
            });
            updateSubscriptionText(subscribeButton);
        } else {
            subscribeButton.setVisibility(View.GONE);
        }

        // Info
        if (infoText != null) {
            Markdown markdown = new Markdown(this);
            markdown.set(infoView, infoText);
        } else {
            infoView.setVisibility(View.GONE);

            // Remove Bottom Margin From Subscribe Button
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) subscribeButton.getLayoutParams();
            layoutParams.setMargins(0, 0, 0, 0);
            subscribeButton.requestLayout();
        }

        // Edge-To-Edge
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemGestures());
            root.setPadding(0, 0, 0, insets.bottom);
            return windowInsets;
        });

        // Create Dialog
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(root);
        dialog.show();
    }

    @Override
    protected boolean canShare() {
        return hasCommunityID;
    }

    private CommunityView infoCommunity = null;

    @Override
    protected void share() {
        Sharing.shareCommunity(this, infoCommunity);
    }
}
