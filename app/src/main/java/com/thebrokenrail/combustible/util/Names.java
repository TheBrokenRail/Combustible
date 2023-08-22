package com.thebrokenrail.combustible.util;

import com.thebrokenrail.combustible.api.method.Community;
import com.thebrokenrail.combustible.api.method.Person;

import okhttp3.HttpUrl;

public class Names {
    public static String getCommunityTitle(Community community) {
        String name = community.title;
        if (!community.local) {
            HttpUrl url = HttpUrl.parse(community.actor_id);
            if (url != null) {
                name += '@' + url.host();
            }
        }
        return name;
    }

    public static String getCommunityName(Community community) {
        String name = community.name;
        if (!community.local) {
            HttpUrl url = HttpUrl.parse(community.actor_id);
            if (url != null) {
                name += '@' + url.host();
            }
        }
        return name;
    }

    public static String getPersonTitle(Person person) {
        if (person.display_name != null) {
            return person.display_name;
        } else {
            return '@' + getPersonName(person);
        }
    }

    public static String getPersonName(Person person) {
        String name = person.name;
        if (!person.local) {
            HttpUrl url = HttpUrl.parse(person.actor_id);
            if (url != null) {
                name += '@' + url.host();
            }
        }
        return name;
    }
}
