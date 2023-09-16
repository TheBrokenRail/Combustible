package com.thebrokenrail.combustible.api.util;

import org.jetbrains.annotations.Nullable;

/**
 * An API method that may use an authentication token.
 * @param <T> The API method's response
 */
public abstract class AuthenticatedMethod<T> extends Method<T> {
    /**
     * An authentication token.
     */
    @SuppressWarnings("unused")
    @Nullable
    public String auth;

    /**
     * Checks if this API method requires an authentication token.
     * @return True if an authentication token is required, false otherwise
     */
    public abstract boolean requiresToken();
}
