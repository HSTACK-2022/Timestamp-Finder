#include <jni.h>
#include <opencv2/opencv.hpp>
#include <string>
#include <iostream>
#include <opencv2/imgproc/imgproc_c.h>

using namespace std;
using namespace cv;

extern "C"
JNIEXPORT double JNICALL
Java_com_example_TimeStampFinder_SceneCut_getPSNR(Mat I1, Mat I2)
{
    Mat s1 = I1.clone();

    printf("##################################");

    absdiff(I1, I2, s1);       // |I1 - I2|
    s1.convertTo(s1, CV_32F);  // cannot make a square on 8 bits
    s1 = s1.mul(s1);           // |I1 - I2|^2
    Scalar s = sum(s1);        // sum elements per channel
    double sse = s.val[0] + s.val[1] + s.val[2]; // sum channels

    if (sse <= 1e-10) // for small values return zero
        return 0;

    double mse = sse / (double)(I1.channels() * I1.total());
    double psnr = 10.0*log10((255 * 255) / mse);
    return psnr;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_TimeStampFinder_SceneCut_saveImage(Mat res) {
    static int i = 0;
    char path[16] = "D:/scene";
    char num[3];
    sprintf(num, "%d", i);    // %d를 지정하여 정수를 문자열로 저장
    strcat(path, num);
    strcat(path, ".jpg");
    printf("%s\n", path);
    imwrite(path, res);
    i++;
}