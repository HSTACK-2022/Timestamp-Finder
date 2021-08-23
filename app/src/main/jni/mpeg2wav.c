// Created by hihi5 on 2021-08-12.
//
#include <jni.h>
#include "libavformat/avformat.h"
#include <android/log.h>

#define LOG_TAG "FFmpegForAndroid"

#define LOGI(...) __android_log_print(4, LOG_TAG, __VA_ARGS__);
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG , "libnav", __VA_ARGS__)
#define LOGE(...) __android_log_print(6, LOG_TAG, __VA_ARGS__);

JNIEXPORT jint JNICALL Java_com_example_TimeStampFinder_NDK_Mpeg2Wav(){

    AVFrame AudioFrame; //디코딩 결과가 저장될 프레임

    if(Packet.stream_index == nAudioStreamIdx){ //전에 저장했던 stream index를 비교해서 audio 패킷을 분류한다.
    int nRemainByte = Packet.size;
    void * pOrgData = Packet.data;

    while (nRemainByte > 0){
        int got_frame = 0;
        avcodec_get_frame_defaults(&AudioFrame);
        int Decoded= avcodec_decode_audio4(pAudioCodecContext, &AudioFrame,&got_frame,&Packet);

        if(nDecoded < 0){ //0보다 작은건 디코딩 실패이므로 패킷 무시
            nRemainByte = 0;
            Packet.data = (uint8_t *)pOrgData;
            av_free_packet(&Packet);
            continue;
        }
        nRemainByte -= nDecoded;
        Packet.size -= nDecoded;
        Packet.data += nDecoded;
        if(nRemainByte <= 0){ //packet 디코딩을 완료하면...
            Packet.data = (uint8_t *)pOrgData;
            av_free_packet(&Packet); //패킷 삭제
            nRemainByte = 0;
        }


        if(got_frame){

            int nDataSize = av_samples_get_buffer_size(NULL, pAudioCodecContext->channels,
            AudioFrame.nb_samples,
            pAudioCodecContext->sample_fmt, 1); //디코딩된 audio data의 길이를 구함
            if(pAudioCodecContext->sample_fmt != AV_SAMPLE_FMT_S16){
                SwrContext * pAudioCvtContext;
                pAudioCvtContext = NULL;
                pAudioCvtContext = swr_alloc_set_opts(pAudioCvtContext, pAudioCodecContext->channel_layout, AV_SAMPLE_FMT_S16,pAudioCodecContext->sample_rate,pAudioCodecContext->channel_layout,	pAudioCodecContext->sample_fmt,pAudioCodecContext->sample_rate,0,0); //SwrContext를 생성한다. 여기서는 singed 16bits로 변환하고자 한다.

                int err;
                if ((err = swr_init(pAudioCvtContext)) < 0) { //초기화
                    if(err == AVERROR(EINVAL))
                        fprintf(stderr, "Failed to initialize the resampling context\n");
                }

                static uint8_t AudioCvtBuffer[AVCODEC_MAX_AUDIO_FRAME_SIZE]; //변환결과 저장되는 버퍼
                uint8_t *out[] = {AudioCvtBuffer};
                const uint8_t *in[] = {AudioFrame.data[0]};

                uint8_t *ain[SWR_CH_MAX];

                if(!av_sample_fmt_is_planar(pAudioCodecContext->sample_fmt)){
                    ain[0] = AudioFrame.data[0];
                }
                else{
                    ain[0] = AudioFrame.data[0]; //8.1ch대비 9개를 만들었다
                    ain[1] = AudioFrame.data[0];
                    ain[2] = AudioFrame.data[0];
                    ain[3] = AudioFrame.data[0];
                    ain[4] = AudioFrame.data[0];
                    ain[5] = AudioFrame.data[0];
                    ain[6] = AudioFrame.data[0];
                    ain[7] = AudioFrame.data[0];
                    ain[8] = AudioFrame.data[0];
                }

                swr_convert(pAudioCvtContext,out,AudioFrame.nb_samples,(const uint8_t *)ain,AudioFrame.nb_samples);  //변환수행!
                int nDataSize2 = av_samples_get_buffer_size(NULL, pAudioCodecContext->channels,AudioFrame.nb_samples,AV_SAMPLE_FMT_S16, 1); //결과 데이터 길이 구하기
                memcpy(pBuffer,AudioCvtBuffer,nDataSize2); //결과 데이터 저장 pBuffer이 결과 값 저장 배열이다
            }
            else{
                memcpy(pBuffer,AudioFrame.data[0],nDataSize); //결과 데이터 저장 pBuffer이 결과 값 저장 배열이다 . 변환하지 않고 그냥 복사
            }
        }
    }
}


