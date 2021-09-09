package com.example.TimeStampFinder;

import android.content.Context;
import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;

import static java.lang.Math.log10;
import static org.opencv.core.CvType.CV_32F;

public class SceneCut {

    private static final String TAG = "OPENCV";

    private static int count = -1;
    private Context context;



    // Video를 재생해 장면이 전환되는 부분만 이미지로 저장하는 코드
    public void Check(String filePath, Context context){
        this.context = context;

    }
}
