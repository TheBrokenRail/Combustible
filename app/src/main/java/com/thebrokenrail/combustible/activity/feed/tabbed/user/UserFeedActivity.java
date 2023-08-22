package com.thebrokenrail.combustible.activity.feed.tabbed.user;

import android.content.Intent;
import android.view.Menu;

import androidx.appcompat.app.ActionBar;
import androidx.lifecycle.ViewModelProvider;

import com.thebrokenrail.combustible.R;
import com.thebrokenrail.combustible.activity.feed.prerequisite.FeedPrerequisite;
import com.thebrokenrail.combustible.activity.feed.prerequisite.FeedPrerequisites;
import com.thebrokenrail.combustible.activity.feed.tabbed.TabbedFeedActivity;
import com.thebrokenrail.combustible.api.method.GetPersonDetailsResponse;
import com.thebrokenrail.combustible.api.method.PersonView;
import com.thebrokenrail.combustible.util.Sharing;
import com.thebrokenrail.combustible.util.Util;

import java.util.Objects;

public class UserFeedActivity extends TabbedFeedActivity {
    public static final String USER_ID_EXTRA = "com.thebrokenrail.combustible.USER_ID_EXTRA";

    private int getUser() {
        Intent intent = getIntent();
        if (!intent.hasExtra(USER_ID_EXTRA)) {
            throw new RuntimeException();
        }
        return intent.getIntExtra(USER_ID_EXTRA, -1);
    }

    @Override
    protected void createTabs() {
        int user = getUser();
        addTab(R.string.posts, new UserPostFeedAdapter(viewPager, connection, new ViewModelProvider(this), "posts", user));
        addTab(R.string.comments, new UserCommentFeedAdapter(viewPager, connection, new ViewModelProvider(this), "comments", user));
    }

    @Override
    protected void addPrerequisites(FeedPrerequisites prerequisites) {
        super.addPrerequisites(prerequisites);

        // Load Username
        int user = getUser();
        ActionBar actionBar = Objects.requireNonNull(getSupportActionBar());
        prerequisites.add(new FeedPrerequisite.User(user));
        prerequisites.listen(prerequisite -> {
            if (prerequisite instanceof FeedPrerequisite.User) {
                GetPersonDetailsResponse getPersonDetailsResponse = ((FeedPrerequisite.User) prerequisite).get();

                // Title
                String title = Util.getPersonTitle(getPersonDetailsResponse.person_view.person);
                actionBar.setTitle(title);
                if (getPersonDetailsResponse.person_view.person.display_name != null) {
                    // Subtitle
                    title = '@' + Util.getPersonName(getPersonDetailsResponse.person_view.person);
                    actionBar.setSubtitle(title);
                }

                // Enable Share Button
                infoPerson = getPersonDetailsResponse.person_view;
                invalidateOptionsMenu();
            }
        });
    }

    private PersonView infoPerson = null;

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean ret = super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.feed_share).setEnabled(infoPerson != null);
        return ret;
    }

    @Override
    protected boolean canShare() {
        return true;
    }

    @Override
    protected void share() {
        Sharing.sharePerson(this, infoPerson);
    }
}
