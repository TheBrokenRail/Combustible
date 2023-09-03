package com.thebrokenrail.combustible.api;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import com.thebrokenrail.combustible.api.method.Constants;
import com.thebrokenrail.combustible.api.method.Login;
import com.thebrokenrail.combustible.api.util.AuthenticatedMethod;
import com.thebrokenrail.combustible.api.util.Method;
import com.thebrokenrail.combustible.api.util.Verifiable;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
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
    final HttpUrl instance;
    final String token;

    private Consumer<Runnable> callbackHelper = Runnable::run;
    private final List<Call> currentCalls = new ArrayList<>();
    private final OkHttpClient client = new OkHttpClient();

    /**
     * Connect to an instance.
     * @param instance The target instance's URL
     * @param token The authentication token to use (or null)
     */
    public Connection(HttpUrl instance, String token) {
        this.instance = instance;
        assert instance != null;
        this.token = token;
    }

    /**
     * Checks if an authentication token is currently set.
     * @return True if an authentication token is set, false otherwise
     */
    public boolean hasToken() {
        return token != null;
    }

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
        // Check Authentication
        if (method instanceof AuthenticatedMethod) {
            AuthenticatedMethod<T> authenticatedMethod = (AuthenticatedMethod<T>) method;
            if (authenticatedMethod.requiresToken() && !hasToken()) {
                throw new RuntimeException();
            }
            authenticatedMethod.auth = token;
        }

        // Verify Method
        if (method instanceof Verifiable) {
            ((Verifiable) method).verify();
        }

        // Build Target URL
        HttpUrl.Builder urlBuilder = instance.newBuilder();
        urlBuilder = urlBuilder.addPathSegments("api/" + Constants.VERSION + "/" + method.getPath());

        // Make Request
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
        Call call = client.newCall(request);
        new ResponseHandler<T>(call, method.getResponseClass(), successCallback, errorCallback) {
            @Override
            protected boolean apiError(String error) {
                // 2FA Token Needed
                if (method instanceof Login && (error.contains("MissingTotpToken") || error.contains("missing_totp_token"))) {
                    // A null Response To Login Signals That 2FA Should Be Used
                    success(null);
                    return true;
                } else {
                    return super.apiError(error);
                }
            }
        };
    }

    /**
     * Cancel all current requests and callbacks.
     */
    public void close() {
        synchronized (currentCalls) {
            for (Call call : currentCalls) {
                call.cancel();
            }
            currentCalls.clear();
        }
        setCallbackHelper(runnable -> {});
    }

    /**
     * Get {@link OkHttpClient}.
     * @return The client used for API calls
     */
    public OkHttpClient getClient() {
        return client;
    }

    class ResponseHandler<T> implements Callback {
        private final Class<T> responseClass;
        private final Consumer<T> successCallback;
        private final Runnable errorCallback;

        ResponseHandler(Call call, Class<T> responseClass, Consumer<T> successCallback, Runnable errorCallback) {
            this.responseClass = responseClass;
            this.successCallback = successCallback;
            this.errorCallback = errorCallback;
            synchronized (currentCalls) {
                currentCalls.add(call);
            }
            call.enqueue(this);
        }

        private void error() {
            callbackHelper.accept(errorCallback);
        }

        protected void success(T obj) {
            callbackHelper.accept(() -> successCallback.accept(obj));
        }

        protected boolean apiError(String error) {
            // Debugging
            System.err.println("API ERROR: " + error);
            return false;
        }

        @Override
        public void onFailure(@NotNull Call call, @NotNull IOException e) {
            e.printStackTrace();

            // Run Error Callback
            synchronized (currentCalls) {
                currentCalls.remove(call);
            }
            error();
        }

        @Override
        public void onResponse(@NotNull Call call, @NotNull Response response) {
            synchronized (currentCalls) {
                currentCalls.remove(call);
            }
            try {
                try (ResponseBody responseBody = response.body()) {
                    // Check Response
                    if (!response.isSuccessful()) {
                        // Run Error Callback
                        if (responseBody != null) {
                            // API Error
                            String error = responseBody.string();
                            if (apiError(error)) {
                                // Error Handled
                                return;
                            }
                        }
                        error();
                        return;
                    }

                    // Skip Deserializing If Method Doesn't Have Response
                    if (responseClass == Object.class) {
                        success(null);
                    }

                    // Deserialize Body
                    Moshi moshi = new Moshi.Builder().build();
                    JsonAdapter<T> jsonAdapter = moshi.adapter(responseClass);
                    assert responseBody != null;
                    T obj = jsonAdapter.fromJson(responseBody.string());

                    // Verify
                    if (obj instanceof Verifiable) {
                        //TODO ((Verifiable) obj).verify();
                    }

                    // Run Callback
                    success(obj);
                }
            } catch (Exception e) {
                e.printStackTrace();

                // Run Error Callback
                error();
            }
        }
    }
}