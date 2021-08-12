package com.example.TimeStampFinder;

public class NDK {
    static {
        System.loadLibrary("sample-ffmpeg");
    }
    public native int scanning(String filepath);
}
