package com.thebrokenrail.combustible.api;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonDataException;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.function.Consumer;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * This class handles communicating with a Lemmy instance.
 */
public class Connection {
    /**
     * The current supported Lemmy API version.
     * @see <a href="https://github.com/LemmyNet/lemmy-js-client/blob/main/src/types/others.ts#L1">Current API Version</a>
     */
    public static final String VERSION = "v3";

    private final HttpUrl instance;

    /**
     * Connect to an instance.
     * @param instance The target instance's URL
     */
    public Connection(HttpUrl instance) {
        this.instance = instance;
    }

    private String token = null;

    /**
     * Sets the authentication token to be used for requests. A null token disables authentication.
     * @param token The new token
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * Checks if an authentication token is currently set.
     * @return True if an authentication token is set, false otherwise
     */
    public boolean hasToken() {
        return token != null;
    }

    private Consumer<Runnable> callbackHelper = Runnable::run;

    /**
     * Sets a new callback helper.
     * @param callbackHelper A consumer that handles executing callbacks
     */
    public void setCallbackHelper(Consumer<Runnable> callbackHelper) {
        this.callbackHelper = callbackHelper;
    }

    /**
     * Run API method.
     * @param method The API method to run
     * @param successCallback Callback that is executed on success
     * @param errorCallback Callback that is executed om failure
     * @param <T> The API method's response
     */
    public <T> void send(Method<T> method, Consumer<T> successCallback, Runnable errorCallback) {
        // Check Instance URL
        if (instance == null) {
            callbackHelper.accept(errorCallback);
            return;
        }

        // Check Authentication
        if (method instanceof AuthenticatedMethod) {
            AuthenticatedMethod<T> authenticatedMethod = (AuthenticatedMethod<T>) method;
            if (authenticatedMethod.requiresToken() && !hasToken()) {
                throw new RuntimeException();
            }
            authenticatedMethod.auth = token;
        }

        // Build Target URL
        HttpUrl.Builder urlBuilder = instance.newBuilder();
        urlBuilder = urlBuilder.addPathSegments("api/" + VERSION + "/" + method.getPath());

        // Make Request
        OkHttpClient client = new OkHttpClient();
        Request.Builder requestBuilder = new Request.Builder();

        // Create POST Request If Needed
        if (method.getType() != Method.Type.GET) {
            // Set URL
            requestBuilder = requestBuilder.url(urlBuilder.build());

            // Serialize Body
            Moshi moshi = new Moshi.Builder().build();
            JsonAdapter<Method<T>> jsonAdapter = moshi.adapter(Types.newParameterizedType(method.getClass(), method.getResponseClass()));
            String json = jsonAdapter.toJson(method);

            // Create Request Body
            MediaType jsonType = MediaType.get("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(json, jsonType);
            if (method.getType() == Method.Type.POST) {
                requestBuilder = requestBuilder.post(body);
            } else {
                requestBuilder = requestBuilder.put(body);
            }
        } else {
            // Add Query Parameters
            try {
                @SuppressWarnings("rawtypes") Class<? extends Method> clazz = method.getClass();
                for (Field field : clazz.getFields()) {
                    int modifiers = field.getModifiers();
                    if (Modifier.isPublic(modifiers) && !Modifier.isStatic(modifiers)) {
                        String name = field.getName();
                        Object obj = field.get(method);
                        if (obj != null) {
                            String value;
                            if (obj instanceof Enum) {
                                value = ((Enum<?>) obj).name();
                            } else {
                                value = String.valueOf(obj);
                            }
                            urlBuilder = urlBuilder.addQueryParameter(name, value);
                        }
                    }
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            // Set URL
            requestBuilder = requestBuilder.url(urlBuilder.build());
        }

        // Finalize Request
        Request request = requestBuilder.build();

        // Send Request
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                // Run Error Callback
                callbackHelper.accept(errorCallback);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    // Check Response
                    if (!response.isSuccessful()) {
                        // Run Error Callback
                        if (responseBody != null) {
                            System.err.println("API ERROR: " + responseBody.string());
                        }
                        callbackHelper.accept(errorCallback);
                        return;
                    }

                    // Deserialize Body
                    Moshi moshi = new Moshi.Builder().build();
                    JsonAdapter<T> jsonAdapter = moshi.adapter(method.getResponseClass());
                    assert responseBody != null;
                    try {
                        T obj = jsonAdapter.fromJson(responseBody.string());

                        // Run Callback
                        callbackHelper.accept(() -> successCallback.accept(obj));
                    } catch (JsonDataException e) {
                        e.printStackTrace();

                        // Run Error Callback
                        callbackHelper.accept(errorCallback);
                    }
                }
            }
        });
    }

    /**
     * An API method.
     * @param <T> The API method's response
     */
    public abstract static class Method<T> {
        /**
         * HTTP request type
         */
        public enum Type {
            GET,
            POST,
            PUT
        }

        /**
         * Retrieves the API method's URL path.
         * @return The method's path after the API version (without a preceding slash), for instance "community/list"
         */
        public abstract String getPath();

        /**
         * Retrieves the API method's HTTP request type
         * @return The HTTP request type
         */
        public Type getType() {
            return Type.GET;
        }

        /**
         * Retrieves the class representing this API method's response.
         * @return The API method response's class
         */
        public abstract Class<T> getResponseClass();
    }

    /**
     * An API method that may use an authentication token.
     * @param <T> The API method's response
     */
    public abstract static class AuthenticatedMethod<T> extends Method<T> {
        /**
         * An authentication token.
         */
        @Nullable
        public String auth;

        /**
         * Checks if this API method requires an authentication token.
         * @return True if an authentication token is required, false otherwise
         */
        public boolean requiresToken() {
            return false;
        }
    }
}