package com.thebrokenrail.combustible.activity.feed.tabbed.inbox;

import androidx.annotation.Nullable;

import com.thebrokenrail.combustible.activity.feed.util.dataset.FeedDataset;
import com.thebrokenrail.combustible.activity.feed.util.dataset.SimpleFeedDataset;
import com.thebrokenrail.combustible.api.method.CommentReplyView;
import com.thebrokenrail.combustible.api.method.CommentView;
import com.thebrokenrail.combustible.api.method.PersonMentionView;

import java.lang.reflect.Field;

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
                newElement = new NotificationCommentView(oldElement, newElement);

                // Call Original Method
                super.replaceInternal(notifier, oldElement, newElement);
            }
        };
    }
}
