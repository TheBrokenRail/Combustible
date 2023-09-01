package com.thebrokenrail.combustible.activity.feed.tabbed.inbox;

import android.content.Context;
import android.view.View;

import androidx.lifecycle.ViewModelProvider;

import com.thebrokenrail.combustible.activity.feed.comment.BaseCommentFeedAdapter;
import com.thebrokenrail.combustible.activity.feed.util.dataset.FeedDataset;
import com.thebrokenrail.combustible.api.Connection;
import com.thebrokenrail.combustible.api.method.CommentSortType;
import com.thebrokenrail.combustible.api.method.CommentView;
import com.thebrokenrail.combustible.api.method.GetPersonMentions;
import com.thebrokenrail.combustible.api.method.MarkPersonMentionAsRead;
import com.thebrokenrail.combustible.api.method.PersonMentionView;
import com.thebrokenrail.combustible.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

class MentionsFeedAdapter extends BaseCommentFeedAdapter {
    MentionsFeedAdapter(View recyclerView, Connection connection, ViewModelProvider viewModelProvider) {
        super(recyclerView, connection, viewModelProvider);
        sorting.set(CommentSortType.New);
    }

    @Override
    protected boolean showCreator() {
        return true;
    }

    @Override
    protected boolean isSortingTypeVisible(Class<? extends Enum<?>> type) {
        return type == CommentSortType.class;
    }

    @Override
    protected void loadPage(int page, Consumer<List<CommentView>> successCallback, Runnable errorCallback) {
        GetPersonMentions method = new GetPersonMentions();
        method.page = page;
        method.limit = Util.ELEMENTS_PER_PAGE;
        method.sort = sorting.get(CommentSortType.class);
        connection.send(method, getPersonMentionsResponse -> {
            List<CommentView> comments = new ArrayList<>();
            for (PersonMentionView mention : getPersonMentionsResponse.mentions) {
                comments.add(new NotificationCommentView(mention));
            }
            successCallback.accept(comments);
        }, errorCallback);
    }

    @Override
    protected boolean showCommunity() {
        return true;
    }

    @Override
    protected FeedDataset<CommentView> createDataset() {
        return NotificationCommentView.createDataset();
    }

    @Override
    protected void click(Context context, CommentView obj) {
        NotificationCommentView.click(context, obj, commentView -> {
            // Click
            MentionsFeedAdapter.super.click(context, commentView);
        }, integer -> {
            // Mark As Read
            MarkPersonMentionAsRead method = new MarkPersonMentionAsRead();
            method.person_mention_id = integer;
            method.read = true;
            return method;
        }, connection, personMentionResponse -> {
            // Wrap
            return new NotificationCommentView(personMentionResponse.person_mention_view);
        }, viewModel.dataset, notifier);
    }

    @Override
    protected boolean isRead(CommentView obj) {
        return ((NotificationCommentView) obj).read;
    }
}
