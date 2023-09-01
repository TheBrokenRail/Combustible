package com.thebrokenrail.combustible.activity.feed.tabbed.inbox;

import android.content.Context;

import androidx.annotation.Nullable;

import com.thebrokenrail.combustible.activity.feed.util.dataset.FeedDataset;
import com.thebrokenrail.combustible.activity.feed.util.dataset.SimpleFeedDataset;
import com.thebrokenrail.combustible.api.Connection;
import com.thebrokenrail.combustible.api.method.CommentReplyView;
import com.thebrokenrail.combustible.api.method.CommentView;
import com.thebrokenrail.combustible.api.method.PersonMentionView;
import com.thebrokenrail.combustible.api.util.Method;
import com.thebrokenrail.combustible.util.Util;

import java.lang.reflect.Field;
import java.util.function.Consumer;
import java.util.function.Function;

class NotificationCommentView extends CommentView {
    final int notification_id;
    final boolean read;

    private static void copyProperties(Object from, NotificationCommentView to) {
        try {
            for (Field targetField : CommentView.class.getDeclaredFields()) {
                String name = targetField.getName();
                Field sourceField = from.getClass().getDeclaredField(name);
                assert sourceField.getType() == targetField.getType();
                targetField.set(to, sourceField.get(from));
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private NotificationCommentView(CommentView oldComment, CommentView newComment) {
        copyProperties(newComment, this);
        notification_id = ((NotificationCommentView) oldComment).notification_id;
        read = ((NotificationCommentView) oldComment).read;
    }

    NotificationCommentView(CommentReplyView reply) {
        copyProperties(reply, this);
        notification_id = reply.comment_reply.id;
        read = reply.comment_reply.read;
    }

    NotificationCommentView(PersonMentionView mention) {
        copyProperties(mention, this);
        notification_id = mention.person_mention.id;
        read = mention.person_mention.read;
    }

    static FeedDataset<CommentView> createDataset() {
        return new SimpleFeedDataset<CommentView>() {
            @Override
            public void replaceInternal(@Nullable Notifier notifier, CommentView oldElement, CommentView newElement) {
                // Copy Notification ID
                if (!(newElement instanceof NotificationCommentView)) {
                    newElement = new NotificationCommentView(oldElement, newElement);
                }

                // Call Original Method
                super.replaceInternal(notifier, oldElement, newElement);
            }
        };
    }

    static <T extends Method<J>, J> void click(Context context, CommentView comment, Consumer<CommentView> clickCallback, Function<Integer, T> createMethod, Connection connection, Function<J, NotificationCommentView> commentExtractor, FeedDataset<CommentView> dataset, FeedDataset.Notifier notifier) {
        NotificationCommentView notification = (NotificationCommentView) comment;
        boolean read = notification.read;
        if (read) {
            clickCallback.accept(comment);
        } else {
            // Mark As Read
            Method<J> method = createMethod.apply(notification.notification_id);
            connection.send(method, j -> {
                // Update Dataset
                NotificationCommentView newNotification = commentExtractor.apply(j);
                dataset.replace(notifier, notification, newNotification);
                // Click
                clickCallback.accept(newNotification);
            }, () -> Util.unknownError(context));
        }
    }
}
