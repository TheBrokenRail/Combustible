package com.thebrokenrail.combustible.util;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.FragmentActivity;

import com.thebrokenrail.combustible.R;
import com.thebrokenrail.combustible.api.Connection;
import com.thebrokenrail.combustible.api.Pictrs;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;
import okio.Okio;

public class Uploader {
    private static final String DEFAULT_TYPE = "application/octet-stream";

    // https://github.com/square/okhttp/issues/3585#issuecomment-327319196
    private static class InputStreamRequestBody extends RequestBody {
        private final MediaType contentType;
        private final ContentResolver contentResolver;
        private final Uri uri;

        private InputStreamRequestBody(MediaType contentType, ContentResolver contentResolver, Uri uri) {
            this.contentType = contentType;
            this.contentResolver = contentResolver;
            this.uri = uri;
        }

        @Nullable
        @Override
        public MediaType contentType() {
            return contentType;
        }

        @Override
        public long contentLength() {
            return -1;
        }

        @Override
        public void writeTo(@NonNull BufferedSink sink) throws IOException {
            sink.writeAll(Okio.source(Objects.requireNonNull(contentResolver.openInputStream(uri))));
        }
    }

    public static void onActivityResult(Context context, Connection connection, int resultCode, Intent data, Consumer<String> successCallback) {
        if (resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            if (uri != null) {
                // Gather Information
                ContentResolver contentResolver = context.getContentResolver();
                DocumentFile doc = DocumentFile.fromSingleUri(context, uri);
                assert doc != null;
                String mimeType = doc.getType();
                if (mimeType == null) {
                    mimeType = DEFAULT_TYPE;
                }
                MediaType type = MediaType.Companion.get(mimeType);

                // Toast
                Toast.makeText(context.getApplicationContext(), R.string.uploading_image, Toast.LENGTH_SHORT).show();

                // Upload Image
                InputStreamRequestBody requestBody = new InputStreamRequestBody(type, contentResolver, uri);
                new Pictrs(connection).upload(doc.getName(), requestBody, successCallback, () -> Util.unknownError(context));
            }
        }
    }

    public static void upload(FragmentActivity context, int requestCode) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        //noinspection deprecation
        context.startActivityForResult(intent, requestCode);
    }
}
