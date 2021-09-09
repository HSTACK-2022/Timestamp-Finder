package com.example.TimeStampFinder;

import android.graphics.Bitmap;
import android.media.Image;

public class ImageData {

    private Bitmap image;
    private String title;
    private int resId;

    public ImageData(Bitmap image, String title) {
        this.image=image;
        this.title = title;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
