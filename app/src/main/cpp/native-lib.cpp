#include <jni.h>
#include <opencv2/opencv.hpp>
#include <string>
#include <iostream>
#include <opencv2/imgproc/imgproc_c.h>

using namespace std;
using namespace cv;

int processToNegative(Mat img_input, Mat &img_result)
{
    cvtColor( img_input, img_result, CV_RGBA2GRAY);
    Mat srcImage = img_result;

//    if(srcImage.empty())
//        LOGD("%s : empty!",__FUNCTION__);

    Mat_<uchar>image(srcImage);
    Mat_<uchar>destImage(srcImage.size());

    for(int y = 0 ; y < image.rows ; y++){
        for(int x = 0  ; x < image.cols; x++){
            uchar r = image(y,x);
            destImage(y,x) = 255 -r;
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