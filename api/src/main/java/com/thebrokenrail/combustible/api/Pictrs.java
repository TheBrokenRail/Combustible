package com.thebrokenrail.combustible.api;

import java.util.List;
import java.util.function.Consumer;

import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * This class handles communicating with a Lemmy Pictrs instance.
 */
public class Pictrs {
    private static class Response {
        private static class Image {
            private static class Details {
                public int width;
                public int height;
                public String content_type;
                public String created_type;
            }

            public String delete_token;
            public String file;
            public Details details;
        }

        public List<Image> files;
        public String msg;
    }

    private final Connection connection;

    public Pictrs(Connection connection) {
        this.connection = connection;
    }

    /**
     * Upload image.
     * @param name The image's name
     * @param data The image's data
     * @param successCallback Callback that is executed on success
     * @param errorCallback Callback that is executed on failure
     */
    public void upload(String name, RequestBody data, Consumer<String> successCallback, Runnable errorCallback) {
        // Build Target URL
        HttpUrl.Builder urlBuilder = connection.instance.newBuilder();
        urlBuilder = urlBuilder.addPathSegments("pictrs/image");

        // Make Request
        OkHttpClient client = new OkHttpClient();
        Request.Builder requestBuilder = new Request.Builder();

        // Set URL
        requestBuilder = requestBuilder.url(urlBuilder.build());

        // Add Image
        RequestBody requestBody = new MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("images[]", name, data)
            .build();
        requestBuilder = requestBuilder.post(requestBody);

        // Add Token
        requestBuilder = requestBuilder.addHeader("Cookie", "jwt=" + connection.token);

        // Finalize Request
        Request request = requestBuilder.build();

        // Send Request
        Call call = client.newCall(request);
        connection.new ResponseHandler<>(call, Response.class, response -> {
            // Check
            assert response.msg.equals("ok");
            assert response.files.size() == 1;
            // Build Target URL
            HttpUrl.Builder urlBuilder1 = connection.instance.newBuilder();
            urlBuilder1 = urlBuilder1.addPathSegments("pictrs/image/" + response.files.get(0).file);
            successCallback.accept(urlBuilder1.build().toString());
        }, errorCallback);
    }
}
