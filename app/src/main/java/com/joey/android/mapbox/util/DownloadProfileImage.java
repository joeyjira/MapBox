package com.joey.android.mapbox.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.Log;

import com.joey.android.mapbox.model.User;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadProfileImage extends AsyncTask<User, Void, Bitmap> {
    private static final String TAG = "DownloadProfileImage";

    User mUser;

    @Override
    protected Bitmap doInBackground(User... users) {
        Log.i(TAG, "Async Task called to download image");
        mUser = users[0];

        String photoUri = mUser.getPhotoUri();
        Bitmap photoBitmap = null;

        try {
            byte[] imageBytes = getUrlBytes(photoUri);
            photoBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch bitmap from url");
        }

        return photoBitmap;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
//        mProfileImage.setImageBitmap(bitmap);
//        Bitmap blurImage = blur(bitmap);
//        mBlurryImage.setImageBitmap(blurImage);
    }

    private static final float BLUR_RADIUS = 25f;

//    public static Bitmap blur(Bitmap image) {
//        if (null == image) return null;
//
//        Bitmap outputBitmap = Bitmap.createBitmap(image);
//        final RenderScript renderScript = RenderScript.create(getActivity());
//        Allocation tmpIn = Allocation.createFromBitmap(renderScript, image);
//        Allocation tmpOut = Allocation.createFromBitmap(renderScript, outputBitmap);
//
//        //Intrinsic Gausian blur filter
//        ScriptIntrinsicBlur theIntrinsic = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript));
//        theIntrinsic.setRadius(BLUR_RADIUS);
//        theIntrinsic.setInput(tmpIn);
//        theIntrinsic.forEach(tmpOut);
//        tmpOut.copyTo(outputBitmap);
//        return outputBitmap;
//    }

    private byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage() +
                        ": with " +
                        urlSpec);
            }

            int bytesRead;
            byte[] buffer = new byte[1024];

            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }

            out.close();
            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }
}