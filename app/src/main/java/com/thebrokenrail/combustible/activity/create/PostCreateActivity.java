package com.thebrokenrail.combustible.activity.create;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.thebrokenrail.combustible.R;
import com.thebrokenrail.combustible.activity.feed.post.PostFeedActivity;
import com.thebrokenrail.combustible.api.Connection;
import com.thebrokenrail.combustible.api.method.CreatePost;
import com.thebrokenrail.combustible.api.method.EditPost;
import com.thebrokenrail.combustible.api.method.GetPost;
import com.thebrokenrail.combustible.api.method.GetPostResponse;
import com.thebrokenrail.combustible.api.method.PostResponse;
import com.thebrokenrail.combustible.api.method.PostView;
import com.thebrokenrail.combustible.util.RequestCodes;
import com.thebrokenrail.combustible.util.Uploader;
import com.thebrokenrail.combustible.util.Util;

import okhttp3.HttpUrl;

public class PostCreateActivity extends BaseCreateActivity<GetPostResponse, PostView> {
    private int communityId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Upload Image
        urlField.setEndIconOnClickListener(v -> {
            // Pick Image
            Uploader.upload(PostCreateActivity.this, RequestCodes.PICK_IMAGE);
        });

        // Community ID
        if (!isEditing) {
            if (!getIntent().hasExtra(PostFeedActivity.COMMUNITY_ID_EXTRA)) {
                throw new RuntimeException();
            }
            communityId = getIntent().getIntExtra(PostFeedActivity.COMMUNITY_ID_EXTRA, -1);
        }

        // Focus
        title.requestFocus();
    }

    @Override
    protected int getActionBarTitle() {
        return isEditing ? R.string.edit_post : R.string.create_post;
    }

    @Override
    protected Connection.Method<GetPostResponse> loadExisting() {
        assert isEditing;
        GetPost method = new GetPost();
        method.id = editId;
        return method;
    }

    @Override
    protected void setFieldsFromExisting(GetPostResponse existing) {
        // Set Fields
        title.setText(existing.post_view.post.name);
        String urlStr = existing.post_view.post.url;
        if (urlStr == null) {
            urlStr = "";
        }
        url.setText(urlStr);
        String bodyStr = existing.post_view.post.body;
        if (bodyStr == null) {
            bodyStr = "";
        }
        body.setText(bodyStr);
        nsfw.setChecked(existing.post_view.post.nsfw);
    }

    @Override
    protected void go() {
        // Gather Information
        String titleStr = String.valueOf(title.getText());
        String urlStr = String.valueOf(url.getText());
        if (urlStr.trim().length() == 0) {
            urlStr = null;
        }
        String bodyStr = String.valueOf(body.getText());
        if (bodyStr.trim().length() == 0) {
            bodyStr = null;
        }
        boolean isNsfw = nsfw.getCheckedState() == MaterialCheckBox.STATE_CHECKED;

        // Check URL
        if (urlStr != null) {
            HttpUrl parsedUrl = HttpUrl.parse(urlStr);
            if (parsedUrl == null) {
                // Error
                urlField.setError(getString(R.string.invalid_url));
                return;
            }
        }

        // Create Method
        Connection.Method<PostResponse> obj;
        if (isEditing) {
            EditPost method = new EditPost();
            method.name = titleStr;
            method.url = urlStr;
            method.body = bodyStr;
            method.nsfw = isNsfw;
            method.post_id = editId;
            obj = method;
        } else {
            CreatePost method = new CreatePost();
            method.name = titleStr;
            method.url = urlStr;
            method.body = bodyStr;
            method.nsfw = isNsfw;
            method.community_id = communityId;
            obj = method;
        }

        // Send
        connection.send(obj, postResponse -> {
            // Success
            success(postResponse.post_view);
        }, () -> {
            // Error
            Util.unknownError(PostCreateActivity.this);
        });
    }

    @Override
    protected Class<PostView> getResponseClass() {
        return PostView.class;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Handle Image
        if (requestCode == RequestCodes.PICK_IMAGE) {
            Uploader.onActivityResult(this, connection, resultCode, data, s -> {
                // Set URL
                url.setText(s);
            });
        }
    }
}
