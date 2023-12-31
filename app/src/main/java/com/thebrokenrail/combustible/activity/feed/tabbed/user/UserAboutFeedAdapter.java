package com.thebrokenrail.combustible.activity.feed.tabbed.user;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.thebrokenrail.combustible.R;
import com.thebrokenrail.combustible.activity.feed.util.adapter.base.FeedAdapter;
import com.thebrokenrail.combustible.api.Connection;
import com.thebrokenrail.combustible.api.method.GetPersonDetails;
import com.thebrokenrail.combustible.api.method.PersonView;
import com.thebrokenrail.combustible.util.Util;
import com.thebrokenrail.combustible.util.glide.GlideApp;
import com.thebrokenrail.combustible.util.glide.GlideUtil;
import com.thebrokenrail.combustible.util.markdown.Markdown;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class UserAboutFeedAdapter extends FeedAdapter<PersonView> {
    private static class UserAboutViewHolder extends RecyclerView.ViewHolder {
        private final ImageView banner;
        private final TextView biography;

        public UserAboutViewHolder(@NonNull View itemView) {
            super(itemView);
            banner = itemView.findViewById(R.id.user_banner);
            biography = itemView.findViewById(R.id.user_biography);
        }
    }

    private final int user;

    public UserAboutFeedAdapter(View parent, Connection connection, ViewModelProvider viewModelProvider, int user) {
        super(parent, connection, viewModelProvider);
        this.user = user;
    }

    @Override
    protected boolean hasHeader() {
        return false;
    }

    @Override
    protected View createHeader(ViewGroup parent) {
        throw new RuntimeException();
    }

    @Override
    protected RecyclerView.ViewHolder createItem(ViewGroup parent) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View root = inflater.inflate(R.layout.user_about, parent, false);
        return new UserAboutViewHolder(root);
    }

    @Override
    protected void bindElement(@NonNull RecyclerView.ViewHolder holder, int position) {
        UserAboutViewHolder userAboutViewHolder = (UserAboutViewHolder) holder;
        PersonView user = viewModel.dataset.get(position);

        // Banner
        String bannerUrl = user.person.banner;
        ImageView banner = userAboutViewHolder.banner;
        RequestManager requestManager = GlideApp.with(userAboutViewHolder.itemView.getContext());
        if (bannerUrl != null) {
            GlideUtil.load(userAboutViewHolder.itemView.getContext(), requestManager, bannerUrl, new FitCenter(), 0, false, false, null, banner);
            banner.setVisibility(View.VISIBLE);
        } else {
            banner.setVisibility(View.GONE);
            requestManager.clear(banner);
        }

        // Build Text
        Resources resources = userAboutViewHolder.itemView.getResources();
        String text = "";

        // Add Account Information
        text += resources.getString(R.string.user_about_account) + '\n';
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withZone(ZoneId.systemDefault());
        Instant timeInstant = Instant.parse(user.person.published + 'Z');
        text += resources.getString(R.string.user_about_account_cake_day, formatter.format(timeInstant)) + '\n';
        if (user.person.matrix_user_id != null) {
            // Matrix User
            String matrixUser = user.person.matrix_user_id;
            text += resources.getString(R.string.user_about_account_matrix_user, matrixUser) + '\n';
        }
        // Posts/Comments
        text += resources.getString(R.string.counts_posts, user.counts.post_count) + '\n';
        text += resources.getString(R.string.counts_comments, user.counts.comment_count) + '\n';

        // Add Karma
        text += '\n' + resources.getString(R.string.user_about_karma) + '\n';
        int postKarma = user.counts.post_score;
        int commentKarma = user.counts.comment_score;
        int totalKarma = postKarma + commentKarma;
        text += resources.getString(R.string.user_about_karma_post, postKarma) + '\n';
        text += resources.getString(R.string.user_about_karma_comment, commentKarma) + '\n';
        text += resources.getString(R.string.user_about_karma_total, totalKarma) + '\n';

        // Add Biography
        String biographyStr = user.person.bio;
        if (biographyStr == null) {
            biographyStr = "";
        }
        biographyStr = biographyStr.trim();
        if (biographyStr.length() > 0) {
            text += '\n' + resources.getString(R.string.user_about_biography) + '\n';
            text += biographyStr;
        }

        // Display Biography
        TextView biography = userAboutViewHolder.biography;
        Markdown markdown = new Markdown(biography.getContext());
        markdown.set(biography, text);
    }

    @Override
    protected void bindHeader(View root) {
        throw new RuntimeException();
    }

    @Override
    protected void loadPage(int page, Consumer<List<PersonView>> successCallback, Runnable errorCallback) {
        GetPersonDetails method = new GetPersonDetails();
        method.person_id = user;
        method.limit = Util.MIN_LIMIT;
        connection.send(method, getPersonDetailsResponse -> {
            // Success
            successCallback.accept(Collections.singletonList(getPersonDetailsResponse.person_view));
        }, errorCallback);
    }

    @Override
    protected boolean isSinglePage() {
        return true;
    }
}
