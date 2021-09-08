package com.example.TimeStampFinder;


import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.videoio.VideoCapture;

import static java.lang.Math.log10;
import static org.opencv.core.CvType.CV_32F;

public class SceneCut {

    private static final String TAG = "OPENCV";

    public native double getPSNR(Mat I1, Mat I2);
    public native void saveImage(Mat res);

    static {
        System.loadLibrary("opencv_java4");
        System.loadLibrary("native-lib");
    }

    public double getP(Mat I1, Mat I2){
        Mat s1 = I1.clone();
        Core.absdiff(I1, I2, s1);

        s1.convertTo(s1, CV_32F);
        s1 = s1.mul(s1);
        Scalar s = Core.sumElems(s1);
        double sse = s.val[0] + s.val[1] + s.val[2];

        Log.d(TAG, "see : "+sse);

        if (sse <= 1e-10)
            return 0;

        double mse = sse / (double)(I1.channels() * I1.total());
        double psnr = 10.0*log10((255*255) / mse);
        return psnr;
    }

    // Video를 재생해 장면이 전환되는 부분만 이미지로 저장하는 코드
    public void Check(String filePath){
        int frameNum = -1;
        double psnrV, CHANGE_DETECT_AUDIO = 15.0;
        VideoCapture cap = new VideoCapture();
        Mat prevFrame = new Mat();
        Mat currFrame = new Mat();
        Mat changeFrame = new Mat();
        Mat result[];

        cap.open(filePath);
        Log.d(TAG, filePath);

        while(cap.read(currFrame)) {
            if (!cap.isOpened())
                Log.d(TAG, "ERROR : OPEN FAILED");

            ++frameNum;
            Log.d(TAG, "frame No. " + frameNum);

            if (frameNum < 1) {
                prevFrame = currFrame.clone();
                changeFrame = currFrame.clone();
                continue;
            }

            if (currFrame.rows() == 0 && currFrame.cols() == 0)
                break;

            psnrV = getPSNR(prevFrame, currFrame);

            Log.d(TAG, "CHECK2 : " + frameNum);

            if (psnrV < CHANGE_DETECT_AUDIO) {
                changeFrame = currFrame.clone();
            }

            Log.d(TAG, "CHECK3 : " + frameNum);

            if (frameNum % 2 != 0)
                prevFrame = currFrame.clone();
        }
    }
}
