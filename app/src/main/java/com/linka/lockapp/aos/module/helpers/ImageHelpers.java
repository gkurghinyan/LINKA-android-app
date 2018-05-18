package com.linka.lockapp.aos.module.helpers;

/**
 * Created by Vanson on 17/11/15.
 */

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.Log;
import android.widget.ImageView;

import com.linka.lockapp.aos.AppDelegate;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by van on 16/7/15.
 */
public class ImageHelpers {

    private static final int MAX_WIDTH = 512;
    private static final int MAX_HEIGHT = 512;

    private static int size = (int) Math.ceil(Math.sqrt(MAX_WIDTH * MAX_HEIGHT));

    public static void showAsync(ImageView imageView, String url) {
        OkHttpClient okHttpClient = new OkHttpClient();
        OkHttpDownloader downloader = new OkHttpDownloader(okHttpClient);
        Picasso.with(imageView.getContext()).cancelRequest(imageView);
        if (url == null)
        {
            imageView.setImageBitmap(null);
            return;
        }
        PicassoCache.myPicassoInstance.with(imageView.getContext()).load(url)
                .into(imageView);
    }


    public static void showAsyncBlurred(ImageView imageView, String url) {
        OkHttpClient okHttpClient = new OkHttpClient();
        OkHttpDownloader downloader = new OkHttpDownloader(okHttpClient);
        Picasso.with(imageView.getContext()).cancelRequest(imageView);
        if (url == null)
        {
            imageView.setImageBitmap(null);
            return;
        }
        PicassoCache.myPicassoInstance.with(imageView.getContext()).load(url)
                .transform(new BlurTransformation(imageView.getContext(), 5))
                .into(imageView);
    }


    public static void showAsyncCenterCrop(ImageView imageView, String url) {
        Picasso.with(imageView.getContext()).cancelRequest(imageView);
        if (url == null)
        {
            imageView.setImageBitmap(null);
            return;
        }
        PicassoCache.myPicassoInstance.with(imageView.getContext()).load(url)
                .fit()
                .centerCrop()
                .noFade()
//                .placeholder(R.drawable.icon_empty_placeholder)
                .into(imageView);
    }

    public static void showAsyncCircleImageView(ImageView imageView, String url) {
        Picasso.with(imageView.getContext()).cancelRequest(imageView);
        if (url == null)
        {
            imageView.setImageBitmap(null);
            return;
        }
        PicassoCache.myPicassoInstance.with(imageView.getContext()).load(url)
                .transform(new CircleTransformation(0))
                .fit()
                .into(imageView);
    }

    public static void showAsyncCenterInside(ImageView imageView, String url) {
        Picasso.with(imageView.getContext()).cancelRequest(imageView);
        if (url == null)
        {
            imageView.setImageBitmap(null);
            return;
        }
        PicassoCache.myPicassoInstance.with(imageView.getContext()).load(url)
                .into(imageView);
    }

    public static void showAsyncResize(ImageView imageView, String url) {
        Picasso.with(imageView.getContext()).cancelRequest(imageView);
        if (url == null)
        {
            imageView.setImageBitmap(null);
            return;
        }
        PicassoCache.myPicassoInstance.with(imageView.getContext()).load(url)
                .transform(new BitmapTransform(1024, 768))
                .into(imageView);
    }


    public static class BitmapTransform implements Transformation {

        int maxWidth;
        int maxHeight;

        public BitmapTransform(int maxWidth, int maxHeight) {
            this.maxWidth = maxWidth;
            this.maxHeight = maxHeight;
        }

        @Override
        public Bitmap transform(Bitmap source) {
            int targetWidth, targetHeight;
            double aspectRatio;

            if (source.getWidth() > source.getHeight() && source.getWidth() > maxWidth) {
                targetWidth = maxWidth;
                aspectRatio = (double) source.getHeight() / (double) source.getWidth();
                targetHeight = (int) (targetWidth * aspectRatio);
                Bitmap result = Bitmap.createScaledBitmap(source, targetWidth, targetHeight, false);
                if (result != source) {
                    source.recycle();
                }
                return result;
            } else if (source.getHeight() > maxHeight) {
                targetHeight = maxHeight;
                aspectRatio = (double) source.getWidth() / (double) source.getHeight();
                targetWidth = (int) (targetHeight * aspectRatio);
                Bitmap result = Bitmap.createScaledBitmap(source, targetWidth, targetHeight, false);
                if (result != source) {
                    source.recycle();
                }
                return result;
            }

            return source;
        }

        @Override
        public String key() {
            return maxWidth + "x" + maxHeight;
        }


    }










    public static class PicassoCache {
        private static Picasso myPicassoInstance = null;

        public static void setPicassoSingleton(Context context) {
            if (myPicassoInstance == null) {
                myPicassoInstance = createMyPicassoInstance(context);
                Picasso.setSingletonInstance(myPicassoInstance);
//                if (BuildConfig.DEBUG) {
//                    Log.i("PICASSO INSTANCE", "CREATED");
//                }
            }
        }

        private static Picasso createMyPicassoInstance(Context context) {
            OkHttpClient myOkHttpClient = new OkHttpClient();
            myOkHttpClient.interceptors().add(new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request newRequest = chain.request().newBuilder()
                            .addHeader("Cookie", "xyz").build();
//                    if (BuildConfig.DEBUG) {
//                    }
                    return chain.proceed(newRequest);
                }
            });

            Picasso built = new Picasso.Builder(context)
                    .downloader(new OkHttpDownloader(AppDelegate.getInstance(), Integer.MAX_VALUE))
                    .build();
            built.setIndicatorsEnabled(false);
            built.setLoggingEnabled(false);
            return built;
        }

    }






    @SuppressLint("NewApi")
    public static Bitmap blurRenderScript(Context context,Bitmap smallBitmap, int radius) {
        try {
            smallBitmap = RGB565toARGB888(smallBitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Bitmap bitmap = Bitmap.createBitmap(
                smallBitmap.getWidth(), smallBitmap.getHeight(),
                Bitmap.Config.ARGB_8888);

        RenderScript renderScript = RenderScript.create(context);

        Allocation blurInput = Allocation.createFromBitmap(renderScript, smallBitmap);
        Allocation blurOutput = Allocation.createFromBitmap(renderScript, bitmap);

        ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(renderScript,
                Element.U8_4(renderScript));
        blur.setInput(blurInput);
        blur.setRadius(radius); // radius must be 0 < r <= 25
        blur.forEach(blurOutput);

        blurOutput.copyTo(bitmap);
        renderScript.destroy();

        return bitmap;

    }

    private static Bitmap RGB565toARGB888(Bitmap img) throws Exception {
        int numPixels = img.getWidth() * img.getHeight();
        int[] pixels = new int[numPixels];

        //Get JPEG pixels.  Each int is the color values for one pixel.
        img.getPixels(pixels, 0, img.getWidth(), 0, 0, img.getWidth(), img.getHeight());

        //Create a Bitmap of the appropriate format.
        Bitmap result = Bitmap.createBitmap(img.getWidth(), img.getHeight(), Bitmap.Config.ARGB_8888);

        //Set RGB pixels.
        result.setPixels(pixels, 0, result.getWidth(), 0, 0, result.getWidth(), result.getHeight());
        return result;
    }



    public static class BlurTransformation implements Transformation {

        private static int MAX_RADIUS = 25;
        private static int DEFAULT_DOWN_SAMPLING = 1;

        private Context mContext;

        private int mRadius;
        private int mSampling;

        public BlurTransformation(Context context) {
            this(context, MAX_RADIUS, DEFAULT_DOWN_SAMPLING);
        }

        public BlurTransformation(Context context, int radius) {
            this(context, radius, DEFAULT_DOWN_SAMPLING);
        }

        public BlurTransformation(Context context, int radius, int sampling) {
            mContext = context.getApplicationContext();
            mRadius = radius;
            mSampling = sampling;
        }

        @Override public Bitmap transform(Bitmap source) {

            int scaledWidth = source.getWidth() / mSampling;
            int scaledHeight = source.getHeight() / mSampling;

            Bitmap bitmap = Bitmap.createBitmap(scaledWidth, scaledHeight, Bitmap.Config.ARGB_8888);

            Canvas canvas = new Canvas(bitmap);
            canvas.scale(1 / (float) mSampling, 1 / (float) mSampling);
            Paint paint = new Paint();
            paint.setFlags(Paint.FILTER_BITMAP_FLAG);
            canvas.drawBitmap(source, 0, 0, paint);

            RenderScript rs = RenderScript.create(mContext);
            Allocation input = Allocation.createFromBitmap(rs, bitmap, Allocation.MipmapControl.MIPMAP_NONE,
                    Allocation.USAGE_SCRIPT);
            Allocation output = Allocation.createTyped(rs, input.getType());
            ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));

            blur.setInput(input);
            blur.setRadius(mRadius);
            blur.forEach(output);
            output.copyTo(bitmap);

            source.recycle();
            rs.destroy();

            return bitmap;
        }

        @Override public String key() {
            return "BlurTransformation(radius=" + mRadius + ", sampling=" + mSampling + ")";
        }
    }




    public static class CircleTransformation implements
            com.squareup.picasso.Transformation {
        private final int margin; // dp

        // radius is corner radii in dp
        // margin is the board in dp
        public CircleTransformation(final int margin) {
            this.margin = margin;
        }

        @Override
        public Bitmap transform(final Bitmap source) {
            final Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setShader(new BitmapShader(source, Shader.TileMode.CLAMP,
                    Shader.TileMode.CLAMP));

            Bitmap output = Bitmap.createBitmap(source.getWidth(),
                    source.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(output);
            canvas.drawRoundRect(new RectF(margin, margin, source.getWidth()
                    - margin, source.getHeight() - margin), source.getWidth() / 2, source.getHeight() / 2, paint);

            if (source != output) {
                source.recycle();
            }

            return output;
        }

        @Override
        public String key() {
            return "rounded";
        }
    }
}