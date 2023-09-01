package com.thebrokenrail.combustible.activity.feed.tabbed.inbox.privatemessage;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.thebrokenrail.combustible.R;
import com.thebrokenrail.combustible.activity.feed.util.adapter.base.FeedAdapter;
import com.thebrokenrail.combustible.api.Connection;
import com.thebrokenrail.combustible.api.method.GetPrivateMessages;
import com.thebrokenrail.combustible.api.method.MarkPrivateMessageAsRead;
import com.thebrokenrail.combustible.api.method.PrivateMessageView;
import com.thebrokenrail.combustible.util.Util;
import com.thebrokenrail.combustible.util.markdown.Markdown;
import com.thebrokenrail.combustible.widget.CommonIcons;
import com.thebrokenrail.combustible.widget.Metadata;

import java.util.List;
import java.util.function.Consumer;

public class PrivateMessageFeedAdapter extends FeedAdapter<PrivateMessageView> {
    private static class PrivateMessageViewHolder extends RecyclerView.ViewHolder {
        private final CardView card;
        private final TextView text;
        private final Metadata metadata;
        private final CommonIcons icons;

        private PrivateMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.comment_card);
            text = itemView.findViewById(R.id.comment_text);
            metadata = itemView.findViewById(R.id.comment_metadata);
            icons = itemView.findViewById(R.id.comment_icons);
            itemView.findViewById(R.id.comment_bottom_bar).setVisibility(View.GONE);
        }
    }

    private final Markdown markdown;

    public PrivateMessageFeedAdapter(View recyclerView, Connection connection, ViewModelProvider viewModelProvider) {
        super(recyclerView, connection, viewModelProvider);
        markdown = new Markdown(recyclerView.getContext());
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
    public RecyclerView.ViewHolder createItem(ViewGroup parent) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View root = inflater.inflate(R.layout.comment, parent, false);
        return new PrivateMessageViewHolder(root);
    }

    @Override
    protected void bindElement(@NonNull RecyclerView.ViewHolder holder, int position) {
        PrivateMessageView obj = viewModel.dataset.get(position);
        PrivateMessageViewHolder privateMessageViewHolder = (PrivateMessageViewHolder) holder;

        // Card
        boolean isUnread = !obj.private_message.read;
        privateMessageViewHolder.card.setOnClickListener(v -> {
            // Mark As Read
            MarkPrivateMessageAsRead method = new MarkPrivateMessageAsRead();
            method.private_message_id = obj.private_message.id;
            method.read = true;
            connection.send(method, privateMessageResponse -> {
                // Update
                viewModel.dataset.replace(notifier, obj, privateMessageResponse.private_message_view);
            }, () -> {
                // Error
                Util.unknownError(holder.itemView.getContext());
            });
        });
        privateMessageViewHolder.card.setClickable(isUnread);
        privateMessageViewHolder.card.setFocusable(isUnread);

        // Text
        markdown.set(privateMessageViewHolder.text, obj.private_message.content.trim());

        // Comment Metadata
        boolean showAvatars = true;
        if (site.my_user != null) {
            showAvatars = site.my_user.local_user_view.local_user.show_avatars;
        }
        boolean isEdited = obj.private_message.updated != null;
        boolean blurNsfw = true;
        if (site.my_user != null) {
            blurNsfw = site.my_user.local_user_view.local_user.blur_nsfw;
        }
        privateMessageViewHolder.metadata.setup(obj.creator, null, isEdited ? obj.private_message.updated : obj.private_message.published, isEdited, blurNsfw, showAvatars);

        // Overflow
        privateMessageViewHolder.icons.overflow.setOnClickListener(v -> new PrivateMessageOverflow(v, connection, obj));

        // Icons
        privateMessageViewHolder.icons.setup(obj.private_message.deleted, false, false, false, false, !obj.private_message.read);
    }

    @Override
    protected void bindHeader(View root) {
        throw new RuntimeException();
    }

    @Override
    protected void loadPage(int page, Consumer<List<PrivateMessageView>> successCallback, Runnable errorCallback) {
        GetPrivateMessages method = new GetPrivateMessages();
        method.page = page;
        method.limit = Util.ELEMENTS_PER_PAGE;
        connection.send(method, privateMessagesResponse -> successCallback.accept(privateMessagesResponse.private_messages), errorCallback);
    }
}
