package com.thebrokenrail.combustible.util;

import com.thebrokenrail.combustible.api.method.Community;
import com.thebrokenrail.combustible.api.method.CommunityModeratorView;
import com.thebrokenrail.combustible.api.method.GetSiteResponse;
import com.thebrokenrail.combustible.api.method.Post;

public class Permissions {
    private GetSiteResponse site = null;

    public void setSite(GetSiteResponse site) {
        this.site = site;
    }

    private boolean isAdmin() {
        // Check If Logged In
        if (site == null || site.my_user == null) {
            return false;
        }

        // Return
        return site.my_user.local_user_view.person.admin;
    }

    private boolean isModerator(Community community) {
        // Check If Logged In
        if (site == null || site.my_user == null) {
            return false;
        }

        // Iterate Moderated Communities
        for (CommunityModeratorView communityModeratorView : site.my_user.moderates) {
            if (communityModeratorView.community.id.equals(community.id)) {
                // Is Moderator
                return true;
            }
        }

        // Not Moderator
        return false;
    }

    public boolean canPost(Community community) {
        // Check If Logged In
        if (site == null || site.my_user == null) {
            return false;
        }

        // Check Community
        if (community.posting_restricted_to_mods) {
            // Restricted To Mods And Admins
            return isAdmin() || isModerator(community);
        } else {
            // Public
            return true;
        }
    }

    public boolean canReply(Post post) {
        // Check If Logged In
        if (site == null || site.my_user == null) {
            return false;
        }

        // Check Post
        return !post.locked;
    }
}
