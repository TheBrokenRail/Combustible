package com.thebrokenrail.combustible.activity.feed.tabbed.blocked;

import android.view.View;

import androidx.lifecycle.ViewModelProvider;

import com.thebrokenrail.combustible.activity.feed.util.adapter.simple.BaseUserFeedAdapter;
import com.thebrokenrail.combustible.api.Connection;
import com.thebrokenrail.combustible.api.method.GetSite;
import com.thebrokenrail.combustible.api.method.PersonBlockView;
import com.thebrokenrail.combustible.api.method.PersonView;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class BlockedUserFeedAdapter extends BaseUserFeedAdapter {
    public BlockedUserFeedAdapter(View parent, Connection connection, ViewModelProvider viewModelProvider) {
        super(parent, connection, viewModelProvider);
    }

    @Override
    protected void loadPage(int page, Consumer<List<PersonView>> successCallback, Runnable errorCallback) {
        GetSite method = new GetSite();
        connection.send(method, getSiteResponse -> {
            // Check
            if (getSiteResponse.my_user == null) {
                errorCallback.run();
                return;
            }

            // Copy Into Dataset
            List<PersonView> users = new ArrayList<>();
            for (PersonBlockView personBlockView : getSiteResponse.my_user.person_blocks) {
                PersonView user = new PersonView();
                user.person = personBlockView.target;
                users.add(user);
            }
            successCallback.accept(users);
        }, errorCallback);
    }

    @Override
    protected boolean isSortingTypeVisible(Class<? extends Enum<?>> type) {
        return false;
    }

    @Override
    protected boolean isSinglePage() {
        return true;
    }

    @Override
    protected boolean hasHeader() {
        return false;
    }
}
