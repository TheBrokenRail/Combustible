package com.thebrokenrail.combustible.activity.feed.util.simple;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import androidx.lifecycle.ViewModelProvider;

import com.thebrokenrail.combustible.activity.feed.tabbed.user.UserFeedActivity;
import com.thebrokenrail.combustible.activity.feed.util.overflow.UserOverflow;
import com.thebrokenrail.combustible.api.Connection;
import com.thebrokenrail.combustible.api.method.PersonView;
import com.thebrokenrail.combustible.util.Names;
import com.thebrokenrail.combustible.widget.CommonIcons;

public abstract class BaseUserFeedAdapter extends SimpleFeedAdapter<PersonView> {
    public BaseUserFeedAdapter(View parent, Connection connection, ViewModelProvider viewModelProvider) {
        super(parent, connection, viewModelProvider);
    }

    @Override
    protected String getName(PersonView obj) {
        return Names.getPersonTitle(obj.person);
    }

    @Override
    protected String getIcon(PersonView obj) {
        return obj.person.avatar;
    }

    @Override
    protected void click(Context context, PersonView obj) {
        Intent intent = new Intent(context, UserFeedActivity.class);
        intent.putExtra(UserFeedActivity.USER_ID_EXTRA, obj.person.id);
        context.startActivity(intent);
    }

    @Override
    protected void setupIcons(CommonIcons icons, PersonView obj) {
        icons.setup(false, false, false, false);
        icons.overflow.setOnClickListener(v -> new UserOverflow(v, connection, obj, personView -> viewModel.dataset.replace(notifier, obj, personView), false));
    }
}
