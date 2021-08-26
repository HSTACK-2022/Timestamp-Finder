#include <jni.h>
#include <stdio.h>
#include <android/log.h>

#include "libavformat/avformat.h"
#include "libavformat/avio.h"

#include "libavcodec/avcodec.h"

#include "libavutil/audio_fifo.h"
#include "libavutil/avassert.h"
#include "libavutil/avstring.h"
#include "libavutil/frame.h"
#include "libavutil/opt.h"

#include "libswresample/swresample.h"

#define LOG_TAG "FFmpegForAndroid"

#define LOGI(...) __android_log_print(4, LOG_TAG, __VA_ARGS__);
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG , "libnav", __VA_ARGS__)
#define LOGE(...) __android_log_print(6, LOG_TAG, __VA_ARGS__);

/* The output bit rate in bit/s */
#define OUTPUT_BIT_RATE 16
/* The number of output channels - monotype */
#define OUTPUT_CHANNELS 1

JNIEXPORT jint JNICALL Java_com_example_TimeStampFinder_NDK_scanning(JNIEnv *env, jobject object, jstring filepath)
{
/*

    const AVFormatContext input_format_context;
    const AVCodecContext input_codec_context;
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
        input_format_context = avFormatContext;

        if(avCodecParameters->codec_type == AVMEDIA_TYPE_VIDEO)
        {
            input_codec_context = avCodecParameters->codec_id
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

    //int transcode_acc(AVFormatContext *input_format_context, AVCodecContext *input_codec_context = NULL,
    transcode_acc(input_format_context, input_codec_context);

    return 0;
    */
}

JNIEXPORT jint JNICALL Java_com_example_TimeStampFinder_NDK_decode_1audio (JNIEnv *env, jobject object, jstring inputFile, jstring outputFile){
    // 아래의 명령어를 수행하는 코드입니다.
    // ffmpeg -i /sdcard/input.mp4 -filter:v crop=300:400:10:20 /sdcard/output.mp4
    char* a0 = "ffmpeg";
    char* a1 = inputFile;       //input file name
    char* a2 = outputFile;      //output file name
    char* argv[3];

    argv[0] = a0;
    argv[1] = a1;
    argv[2] = a2;

    LOGI("call decode-audio");
    decode_audio(3, &argv);
}

JNIEXPORT jint JNICALL Java_com_example_TimeStampFinder_NDK_decode_1video (JNIEnv *env, jobject object, jstring inputFile, jstring outputFile){
    // 아래의 명령어를 수행하는 코드입니다.
    // ffmpeg -i /sdcard/input.mp4 -filter:v crop=300:400:10:20 /sdcard/output.mp4
    char* a0 = "ffmpeg";
    char* a1 = inputFile;       //input file name
    char* a2 = outputFile;      //output file name
    char* argv[3];

    argv[0] = a0;
    argv[1] = a1;
    argv[2] = a2;

    LOGI("call decode-video");
    decode_video(3, &argv);
}