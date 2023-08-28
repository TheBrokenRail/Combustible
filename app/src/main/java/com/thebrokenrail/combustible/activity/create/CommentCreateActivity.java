package com.thebrokenrail.combustible.activity.create;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.thebrokenrail.combustible.R;
import com.thebrokenrail.combustible.activity.feed.comment.CommentFeedActivity;
import com.thebrokenrail.combustible.api.method.CommentResponse;
import com.thebrokenrail.combustible.api.method.CommentView;
import com.thebrokenrail.combustible.api.method.CreateComment;
import com.thebrokenrail.combustible.api.method.EditComment;
import com.thebrokenrail.combustible.api.method.GetComment;
import com.thebrokenrail.combustible.api.util.Method;
import com.thebrokenrail.combustible.util.Util;

public class CommentCreateActivity extends BaseCreateActivity<CommentResponse, CommentView> {
    private int postId;
    private boolean hasParentComment;
    private int parentCommentId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hide Unnecessary Fields
        titleField.setVisibility(View.GONE);
        urlField.setVisibility(View.GONE);
        nsfw.setVisibility(View.GONE);

        // Change Hint
        bodyField.setHint(R.string.create_comment_content);

        // Extra Data
        if (!isEditing) {
            if (!getIntent().hasExtra(CommentFeedActivity.POST_ID_EXTRA)) {
                throw new RuntimeException();
            }
            postId = getIntent().getIntExtra(CommentFeedActivity.POST_ID_EXTRA, -1);
            hasParentComment = getIntent().hasExtra(CommentFeedActivity.COMMENT_ID_EXTRA);
            if (hasParentComment) {
                parentCommentId = getIntent().getIntExtra(CommentFeedActivity.COMMENT_ID_EXTRA, -1);
            }
        }

        // Focus
        body.requestFocus();
    }

    @Override
    protected int getActionBarTitle() {
        return isEditing ? R.string.edit_comment : R.string.reply;
    }

    @Override
    protected Method<CommentResponse> loadExisting() {
        assert isEditing;
        GetComment method = new GetComment();
        method.id = editId;
        return method;
    }

    @Override
    protected void setFieldsFromExisting(CommentResponse existing) {
        body.setText(existing.comment_view.comment.content);
    }

    @Override
    protected void go() {
        // Gather Information
        String contentStr = String.valueOf(body.getText());

        // Create Method
        Method<CommentResponse> obj;
        if (isEditing) {
            EditComment method = new EditComment();
            method.content = contentStr;
            method.comment_id = editId;
            obj = method;
        } else {
            CreateComment method = new CreateComment();
            method.content = contentStr;
            method.post_id = postId;
            if (hasParentComment) {
                method.parent_id = parentCommentId;
            }
            obj = method;
        }

        // Send
        connection.send(obj, commentResponse -> {
            // Success
            success(commentResponse.comment_view);
        }, () -> {
            // Error
            Util.unknownError(CommentCreateActivity.this);
        });
    }

    @Override
    protected Class<CommentView> getResponseClass() {
        return CommentView.class;
    }
}
