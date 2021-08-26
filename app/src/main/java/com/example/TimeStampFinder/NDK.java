package com.example.TimeStampFinder;

public class NDK {
    static {
        System.loadLibrary("sample-ffmpeg");
    }
    public native int scanning(String filepath);
    public native int decode_audio(String inputFile, String outputFile);
    public native int decode_video(String inputFile, String outputFile);
}
