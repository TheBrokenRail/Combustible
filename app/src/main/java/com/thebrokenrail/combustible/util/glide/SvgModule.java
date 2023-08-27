package com.thebrokenrail.combustible.util.glide;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.ResourceDecoder;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.resource.bitmap.BitmapResource;
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy;
import com.bumptech.glide.module.AppGlideModule;
import com.bumptech.glide.request.target.Target;
import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

// Based On https://github.com/coil-kt/coil/blob/342c72e648fa8c5c3b64bf6fdb62c7c3af0ccd34/coil-svg/src/main/java/coil/decode/SvgDecoder.kt
@GlideModule
public class SvgModule extends AppGlideModule {
    private final float DEFAULT_SIZE = 512;

    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
        registry.append(InputStream.class, Bitmap.class, new ResourceDecoder<InputStream, Bitmap>() {
            @Override
            public boolean handles(@NonNull InputStream source, @NonNull Options options) throws IOException {
                // Search For "<svg" In 1st KB Of Data
                int size = 1024;
                source.mark(size);
                byte[] data = new byte[size];
                int dataRead = source.read(data);
                if (dataRead == -1) {
                    return false;
                }
                source.reset();
                String str = new String(data, 0, dataRead, StandardCharsets.UTF_8);
                return str.contains("<svg");
            }

            @NonNull
            @Override
            public Resource<Bitmap> decode(@NonNull InputStream source, int width, int height, @NonNull Options options) throws IOException {
                try {
                    // Parse
                    SVG svg = SVG.getFromInputStream(source);

                    // Dimensions
                    RectF viewBox = svg.getDocumentViewBox();
                    float svgWidth;
                    float svgHeight;
                    if (viewBox != null) {
                        svgWidth = viewBox.width();
                        svgHeight = viewBox.height();
                    } else {
                        svgWidth = svg.getDocumentWidth();
                        svgHeight = svg.getDocumentHeight();
                    }

                    // Check Dimensions
                    if (svgWidth <= 0) {
                        svgWidth = DEFAULT_SIZE;
                    }
                    if (svgHeight <= 0) {
                        svgHeight = DEFAULT_SIZE;
                    }

                    // Enable Scaling
                    if (viewBox == null) {
                        svg.setDocumentViewBox(0, 0, svgWidth, svgHeight);
                    }
                    svg.setDocumentWidth("100%");
                    svg.setDocumentHeight("100%");

                    // Scale Dimensions
                    if (width == Target.SIZE_ORIGINAL) {
                        width = (int) svgWidth;
                    }
                    if (height == Target.SIZE_ORIGINAL) {
                        height = (int) svgHeight;
                    }
                    DownsampleStrategy downsampleStrategy = options.get(DownsampleStrategy.OPTION);
                    assert downsampleStrategy != null;
                    float scale = downsampleStrategy.getScaleFactor((int) svgWidth, (int) svgHeight, width, height);
                    svgWidth *= scale;
                    svgHeight *= scale;

                    // Convert To Bitmap
                    Bitmap bitmap = Bitmap.createBitmap((int) svgWidth, (int) svgHeight, Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(bitmap);
                    svg.renderToCanvas(canvas);
                    return BitmapResource.obtain(bitmap, Glide.get(context).getBitmapPool());
                } catch (SVGParseException ex) {
                    throw new IOException("Cannot load SVG from stream", ex);
                }
            }
        });
    }

    @Override
    public boolean isManifestParsingEnabled() {
        return false;
    }
}
