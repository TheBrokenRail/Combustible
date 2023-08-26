package com.thebrokenrail.combustible.activity.feed.tabbed.user;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.lifecycle.ViewModelProvider;

import com.thebrokenrail.combustible.R;
import com.thebrokenrail.combustible.activity.feed.tabbed.TabbedFeedActivity;
import com.thebrokenrail.combustible.activity.feed.util.prerequisite.FeedPrerequisite;
import com.thebrokenrail.combustible.activity.feed.util.prerequisite.FeedPrerequisites;
import com.thebrokenrail.combustible.api.method.BlockPerson;
import com.thebrokenrail.combustible.api.method.GetPersonDetailsResponse;
import com.thebrokenrail.combustible.api.method.GetSiteResponse;
import com.thebrokenrail.combustible.api.method.PersonBlockView;
import com.thebrokenrail.combustible.api.method.PersonView;
import com.thebrokenrail.combustible.util.Names;
import com.thebrokenrail.combustible.util.Sharing;
import com.thebrokenrail.combustible.util.Util;

import java.util.Objects;

public class UserFeedActivity extends TabbedFeedActivity {
    public static final String USER_ID_EXTRA = "com.thebrokenrail.combustible.USER_ID_EXTRA";

    private PersonView infoPerson = null;
    private Boolean isBlocked = null;
    private boolean canPrivateMessage = false;

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
        addTab(R.string.posts, new UserPostFeedAdapter(viewPager, connection, new ViewModelProvider(this), user));
        addTab(R.string.comments, new UserCommentFeedAdapter(viewPager, connection, new ViewModelProvider(this), user));
        addHiddenTab(R.string.communities, new UserCommunityFeedAdapter(viewPager, connection, new ViewModelProvider(this), user));
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
                String title = Names.getPersonTitle(getPersonDetailsResponse.person_view.person);
                actionBar.setTitle(title);
                if (getPersonDetailsResponse.person_view.person.display_name != null) {
                    // Subtitle
                    title = '@' + Names.getPersonName(getPersonDetailsResponse.person_view.person);
                    actionBar.setSubtitle(title);
                }

                // Enable Share Button
                infoPerson = getPersonDetailsResponse.person_view;
                invalidateOptionsMenu();
            } else if (prerequisite instanceof FeedPrerequisite.Site) {
                GetSiteResponse getSiteResponse = ((FeedPrerequisite.Site) prerequisite).get();

                // Communities Tab
                if (getSiteResponse.my_user != null) {
                    if (getSiteResponse.my_user.local_user_view.person.id == user) {
                        showTab(R.string.communities);
                    } else {
                        // Admins Can't Be Blocked
                        boolean isAdmin = false;
                        for (PersonView admin : getSiteResponse.admins) {
                            if (admin.person.id.equals(user)) {
                                isAdmin = true;
                                break;
                            }
                        }

                        // Block User
                        if (!isAdmin) {
                            isBlocked = false;
                            for (PersonBlockView personBlockView : getSiteResponse.my_user.person_blocks) {
                                if (personBlockView.target.id.equals(user)) {
                                    isBlocked = true;
                                    break;
                                }
                            }
                            invalidateOptionsMenu();
                        }

                        // Enable Private Messaging
                        canPrivateMessage = true;
                        invalidateOptionsMenu();
                    }
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean ret = super.onCreateOptionsMenu(menu);
        menu.findItem(R.id.feed_block_user).setVisible(connection.hasToken());
        menu.findItem(R.id.feed_private_message).setVisible(connection.hasToken());
        menu.findItem(R.id.feed_private_message).setEnabled(canPrivateMessage);
        return ret;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean ret = super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.feed_share).setEnabled(infoPerson != null);
        MenuItem blockUser = menu.findItem(R.id.feed_block_user);
        blockUser.setEnabled(isBlocked != null);
        blockUser.setTitle((isBlocked != null && isBlocked) ? R.string.post_unblock_user : R.string.post_block_user);
        return ret;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.feed_block_user) {
            // Block User
            BlockPerson method = new BlockPerson();
            method.person_id = getUser();
            method.block = !isBlocked;
            connection.send(method, blockPersonResponse -> fullRecreate(), () -> Util.unknownError(UserFeedActivity.this));
            return true;
        } else if (item.getItemId() == R.id.feed_private_message) {
            // Private Message
            new PrivateMessageDialogFragment(getUser()).show(getSupportFragmentManager(), "private_message");
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
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
