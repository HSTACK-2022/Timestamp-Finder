#include <jni.h>
#include <opencv2/opencv.hpp>
#include <string>
#include <iostream>
#include <opencv2/imgproc/imgproc_c.h>

using namespace std;
using namespace cv;

int processToNegative(Mat img_input, Mat &img_result)
{
    cvtColor( img_input, img_result, IMREAD_COLOR);
    Mat srcImage = img_result;

    Mat_<Vec3b>image(srcImage);
    Mat_<Vec3b>destImage(srcImage.size());

    for (int y = 0; y < img_input.rows; ++y)
    {
        for (int x = 0; x < img_input.cols; ++x)
        {
            // Blue와 Red값을 바꿔 저장 - 반전 방지
            destImage.at<Vec3b>(y, x)[0] = image.at<Vec3b>(y, x)[2];
            destImage.at<Vec3b>(y, x)[1] = image.at<Vec3b>(y, x)[1];
            destImage.at<Vec3b>(y, x)[2] = image.at<Vec3b>(y, x)[0];
        }
    }

    img_result = destImage.clone();
    return(0);
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_example_TimeStampFinder_StreamFragment_convertNativeLibtoNegative(JNIEnv *env,
                                                                           jobject thiz,
                                                                           jlong addr_input,
                                                                           jlong addr_result) {
    // TODO: implement convertNativeLibtoNegative()
    Mat &img_input = *(Mat *) addr_input;
    Mat &img_result = *(Mat *) addr_result;

    int conv = processToNegative(img_input, img_result);
    int ret = (jint) conv;

    return ret;
}