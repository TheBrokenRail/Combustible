package com.thebrokenrail.combustible.activity.feed.tabbed.inbox;

import android.content.Context;
import android.view.View;

import androidx.lifecycle.ViewModelProvider;

import com.thebrokenrail.combustible.activity.feed.comment.BaseCommentFeedAdapter;
import com.thebrokenrail.combustible.activity.feed.util.dataset.FeedDataset;
import com.thebrokenrail.combustible.api.Connection;
import com.thebrokenrail.combustible.api.method.CommentReplyView;
import com.thebrokenrail.combustible.api.method.CommentSortType;
import com.thebrokenrail.combustible.api.method.CommentView;
import com.thebrokenrail.combustible.api.method.GetReplies;
import com.thebrokenrail.combustible.api.method.MarkCommentReplyAsRead;
import com.thebrokenrail.combustible.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

class RepliesFeedAdapter extends BaseCommentFeedAdapter {
    RepliesFeedAdapter(View recyclerView, Connection connection, ViewModelProvider viewModelProvider) {
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
        GetReplies method = new GetReplies();
        method.page = page;
        method.limit = Util.ELEMENTS_PER_PAGE;
        method.sort = sorting.get(CommentSortType.class);
        connection.send(method, getRepliesResponse -> {
            List<CommentView> comments = new ArrayList<>();
            for (CommentReplyView reply : getRepliesResponse.replies) {
                comments.add(new NotificationCommentView(reply));
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
            RepliesFeedAdapter.super.click(context, commentView);
        }, integer -> {
            // Mark As Read
            MarkCommentReplyAsRead method = new MarkCommentReplyAsRead();
            method.comment_reply_id = integer;
            method.read = true;
            return method;
        }, connection, commentReplyResponse -> {
            // Wrap
            return new NotificationCommentView(commentReplyResponse.comment_reply_view);
        }, viewModel.dataset, notifier);
    }

    @Override
    protected boolean isRead(CommentView obj) {
        return ((NotificationCommentView) obj).read;
    }
}
