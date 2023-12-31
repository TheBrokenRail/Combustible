package com.thebrokenrail.combustible.activity.feed.util.prerequisite;

import com.thebrokenrail.combustible.api.method.CommentResponse;
import com.thebrokenrail.combustible.api.method.GetComment;
import com.thebrokenrail.combustible.api.method.GetCommunity;
import com.thebrokenrail.combustible.api.method.GetCommunityResponse;
import com.thebrokenrail.combustible.api.method.GetPersonDetails;
import com.thebrokenrail.combustible.api.method.GetPersonDetailsResponse;
import com.thebrokenrail.combustible.api.method.GetPost;
import com.thebrokenrail.combustible.api.method.GetPostResponse;
import com.thebrokenrail.combustible.api.method.GetSite;
import com.thebrokenrail.combustible.api.method.GetSiteResponse;
import com.thebrokenrail.combustible.api.method.GetUnreadCount;
import com.thebrokenrail.combustible.api.method.GetUnreadCountResponse;
import com.thebrokenrail.combustible.api.util.Method;
import com.thebrokenrail.combustible.util.Util;

/**
 * Utility class representing a single feed prerequisite.
 * @param <T> The type of the prerequisite data
 */
public abstract class FeedPrerequisite<T> {
    T value = null;

    FeedPrerequisite() {}

    /**
     * Get the data.
     * @return The data (or null if it hasn't loaded yet)
     */
    public final T get() {
        return value;
    }

    /**
     * Select the API method used to load the data
     * @return The API method
     */
    protected abstract Method<T> prepare();

    /**
     * Retrieve general instance information.
     */
    public final static class Site extends FeedPrerequisite<GetSiteResponse> {
        public Site() {
            super();
        }

        @Override
        protected Method<GetSiteResponse> prepare() {
            return new GetSite();
        }
    }

    /**
     * Retrieve post information.
     */
    public final static class Post extends FeedPrerequisite<GetPostResponse> {
        private final Integer id;
        private final Integer comment_id;

        public Post(Integer id, Integer comment_id) {
            super();
            this.id = id;
            this.comment_id = comment_id;
            // Ensure only one ID is defined
            assert (id != null) ^ (comment_id != null);
        }

        @Override
        protected Method<GetPostResponse> prepare() {
            GetPost method = new GetPost();
            method.id = id;
            method.comment_id = comment_id;
            return method;
        }
    }

    /**
     * Retrieve post information.
     */
    public final static class Comment extends FeedPrerequisite<CommentResponse> {
        private final int id;

        public Comment(int id) {
            super();
            this.id = id;
        }

        @Override
        protected Method<CommentResponse> prepare() {
            GetComment method = new GetComment();
            method.id = id;
            return method;
        }
    }

    /**
     * Retrieve community information.
     */
    public final static class Community extends FeedPrerequisite<GetCommunityResponse> {
        private final int id;

        public Community(int id) {
            super();
            this.id = id;
        }

        @Override
        protected Method<GetCommunityResponse> prepare() {
            GetCommunity method = new GetCommunity();
            method.id = id;
            return method;
        }
    }

    /**
     * Retrieve user information.
     */
    public final static class User extends FeedPrerequisite<GetPersonDetailsResponse> {
        private final int id;

        public User(int id) {
            super();
            this.id = id;
        }

        @Override
        protected Method<GetPersonDetailsResponse> prepare() {
            GetPersonDetails method = new GetPersonDetails();
            method.person_id = id;
            method.limit = Util.MIN_LIMIT;
            return method;
        }
    }

    /**
     * Retrieve number of unread notifications.
     */
    public final static class UnreadCount extends FeedPrerequisite<GetUnreadCountResponse> {
        public UnreadCount() {
            super();
        }

        @Override
        protected Method<GetUnreadCountResponse> prepare() {
            return new GetUnreadCount();
        }
    }
}
