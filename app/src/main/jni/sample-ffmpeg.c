#include <jni.h>
#include "libavformat/avformat.h"
#include <android/log.h>

#define LOG_TAG "FFmpegForAndroid"

#define LOGI(...) __android_log_print(4, LOG_TAG, __VA_ARGS__);
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG , "libnav", __VA_ARGS__)
#define LOGE(...) __android_log_print(6, LOG_TAG, __VA_ARGS__);


JNIEXPORT jint JNICALL Java_com_example_TimeStampFinder_NDK_scanning(JNIEnv *env, jobject object, jstring filepath)
{

    const char* nativeFilepath = (*env)->GetStringUTFChars( env, filepath , NULL ) ;
    //const char* nativeFilepath = "/storage/9016-4EF8/aaa.mkv";

    AVFormatContext* avFormatContext = NULL;

    // muxer, demuxer, decoder, encoder 초기화
   av_register_all();

    // nativeFilepath로 avFormatContext 가져오기

    if(avformat_open_input(&avFormatContext, nativeFilepath, NULL, NULL) < 0)
    {
        LOGE("Can't open input file '%s'", nativeFilepath);
        (*env)->ReleaseStringUTFChars(env, filepath, nativeFilepath);
        return -1;
    }

    // 유효한 스트림 정보 찾기
    if(avformat_find_stream_info(avFormatContext, NULL) < 0)
    {
        LOGE("Failed to retrieve input stream information");
        (*env)->ReleaseStringUTFChars(env, filepath, nativeFilepath);
        return -2;
    }

    // avFormatContext->nb_streams : 비디오 파일의 전체 스트림 수
    for(unsigned int index = 0; index < avFormatContext->nb_streams; index++)
    {
        AVCodecParameters* avCodecParameters = avFormatContext->streams[index]->codecpar;
        if(avCodecParameters->codec_type == AVMEDIA_TYPE_VIDEO)
        {
            LOGI("------- Video info -------");
            LOGI("codec_id : %d", avCodecParameters->codec_id);
            LOGI("bitrate : %lld", avCodecParameters->bit_rate);
            LOGI("width : %d / height : %d", avCodecParameters->width, avCodecParameters->height);
        }
        else if(avCodecParameters->codec_type == AVMEDIA_TYPE_AUDIO)
        {
            LOGI("------- Audio info -------");
            LOGI("codec_id : %d", avCodecParameters->codec_id);
            LOGI("bitrate : %lld", avCodecParameters->bit_rate);
            LOGI("sample_rate : %d", avCodecParameters->sample_rate);
            LOGI("number of channels : %d", avCodecParameters->channels);
        }
    }

    // release
    if(avFormatContext != NULL)
    {
        avformat_close_input(&avFormatContext);
    }

    // release
    (*env)->ReleaseStringUTFChars(env, filepath, nativeFilepath);

    return 0;
}