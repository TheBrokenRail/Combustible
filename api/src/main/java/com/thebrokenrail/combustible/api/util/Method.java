package com.thebrokenrail.combustible.api.util;

/**
 * An API method.
 * @param <T> The API method's response
 */
public abstract class Method<T> {
    /**
     * HTTP request type
     */
    public enum Type {
        GET,
        POST,
        PUT
    }

    /**
     * Retrieve the API method's URL path.
     * @return The method's path after the API version (without a preceding slash), for instance "community/list"
     */
    public abstract String getPath();

    /**
     * Retrieve the API method's HTTP request type.
     * @return The HTTP request type
     */
    public Type getType() {
        return Type.GET;
    }

    /**
     * Retrieve the class representing this API method's response.
     * @return The API method response's class
     */
    public abstract Class<T> getResponseClass();
}
