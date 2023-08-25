package com.thebrokenrail.combustible.util;

import com.thebrokenrail.combustible.api.method.CommentView;
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

    private boolean isModerator(int communityId) {
        // Check If Logged In
        if (site == null || site.my_user == null) {
            return false;
        }

        // Iterate Moderated Communities
        for (CommunityModeratorView communityModeratorView : site.my_user.moderates) {
            if (communityModeratorView.community.id.equals(communityId)) {
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
            return isAdmin() || isModerator(community.id);
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

    public boolean canDelete(Post post) {
        // Check If Logged In
        if (site == null || site.my_user == null) {
            return false;
        }

        // Check If Author
        return post.creator_id.equals(site.my_user.local_user_view.person.id);
    }

    public boolean canDelete(CommentView comment) {
        // Check If Logged In
        if (site == null || site.my_user == null) {
            return false;
        }

        // Check If Author
        return comment.comment.creator_id.equals(site.my_user.local_user_view.person.id);
    }

    public boolean canRemove(Post post) {
        // Check If Logged In
        if (site == null || site.my_user == null) {
            return false;
        }

        // Check If Moderator/Admin
        return isAdmin() || isModerator(post.community_id);
    }

    public boolean canRemove(CommentView comment) {
        // Check If Logged In
        if (site == null || site.my_user == null) {
            return false;
        }

        // Check If Moderator/Admin
        return isAdmin() || isModerator(comment.community.id);
    }

    public boolean canDistinguish(CommentView comment) {
        // Check If Logged In
        if (site == null || site.my_user == null) {
            return false;
        }

        // Check If Not Author
        if (!comment.comment.creator_id.equals(site.my_user.local_user_view.person.id)) {
            return false;
        }

        // Check If Moderator/Admin
        return isAdmin() || isModerator(comment.community.id);
    }

    public boolean canLock(Post post) {
        return canRemove(post);
    }

    public boolean canPinCommunity(Post post) {
        return canRemove(post);
    }

    public boolean canPinInstance() {
        // Check If Logged In
        if (site == null || site.my_user == null) {
            return false;
        }

        // Check If Moderator/Admin
        return isAdmin();
    }
}
