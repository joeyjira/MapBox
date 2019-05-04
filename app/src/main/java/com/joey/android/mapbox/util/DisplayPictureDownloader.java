package com.joey.android.mapbox.util;

import android.os.HandlerThread;

public class DisplayPictureDownloader<T> extends HandlerThread {

    public DisplayPictureDownloader() {
        super("DisplayPictureDownloader");
    }

    @Override
    protected void onLooperPrepared() {

    }
}
