package com.thebrokenrail.combustible.util.info;

import android.content.res.Resources;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.thebrokenrail.combustible.R;
import com.thebrokenrail.combustible.api.method.CommunityAggregates;
import com.thebrokenrail.combustible.api.method.CommunityView;
import com.thebrokenrail.combustible.api.method.GetSiteResponse;
import com.thebrokenrail.combustible.api.method.SiteAggregates;

abstract class CommunityOrSiteCounts {
    private enum Type {
        SUBSCRIBERS(R.string.counts_subscribers),
        USERS(R.string.counts_users),
        USERS_PER_DAY(R.string.counts_users_per_day),
        USERS_PER_WEEK(R.string.counts_users_per_week),
        USERS_PER_MONTH(R.string.counts_users_per_month),
        USERS_PER_SIX_MONTHS(R.string.counts_users_per_six_months),
        POSTS(R.string.counts_posts),
        COMMENTS(R.string.counts_comments);

        @StringRes
        private final int resource;

        Type(int resource) {
            this.resource = resource;
        }
    }

    private final Resources resources;
    private CommunityOrSiteCounts(Resources resources) {
        this.resources = resources;
    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        // Iterate Through All Types
        for (Type type : Type.values()) {
            Integer value = get(type);
            if (value != null) {
                // Add To Output
                out.append(resources.getString(type.resource, value));
                out.append('\n');
            }
        }
        // Trim And Return
        return out.toString().trim();
    }

    @Nullable
    protected abstract Integer get(Type type);

    final static class Site extends CommunityOrSiteCounts {
        private final SiteAggregates counts;

        Site(Resources resources, GetSiteResponse site) {
            super(resources);
            this.counts = site.site_view.counts;
        }

        @Nullable
        @Override
        protected Integer get(Type type) {
            switch (type) {
                case USERS: {
                    return counts.users;
                }
                case USERS_PER_DAY: {
                    return counts.users_active_day;
                }
                case USERS_PER_WEEK: {
                    return counts.users_active_week;
                }
                case USERS_PER_MONTH: {
                    return counts.users_active_month;
                }
                case USERS_PER_SIX_MONTHS: {
                    return counts.users_active_half_year;
                }
                case POSTS: {
                    return counts.posts;
                }
                case COMMENTS: {
                    return counts.comments;
                }
                default: {
                    return null;
                }
            }
        }
    }

    final static class Community extends CommunityOrSiteCounts {
        private final CommunityAggregates counts;

        Community(Resources resources, CommunityView community) {
            super(resources);
            this.counts = community.counts;
        }

        @Nullable
        @Override
        protected Integer get(Type type) {
            switch (type) {
                case SUBSCRIBERS: {
                    return counts.subscribers;
                }
                case USERS_PER_DAY: {
                    return counts.users_active_day;
                }
                case USERS_PER_WEEK: {
                    return counts.users_active_week;
                }
                case USERS_PER_MONTH: {
                    return counts.users_active_month;
                }
                case USERS_PER_SIX_MONTHS: {
                    return counts.users_active_half_year;
                }
                case POSTS: {
                    return counts.posts;
                }
                case COMMENTS: {
                    return counts.comments;
                }
                default: {
                    return null;
                }
            }
        }
    }
}
