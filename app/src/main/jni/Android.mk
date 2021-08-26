LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE    := sample-ffmpeg
LOCAL_SRC_FILES := sample-ffmpeg.c decode-audio.c decode-video.c
LOCAL_LDLIBS := -llog
LOCAL_SHARED_LIBRARIES := libavcodec libavdevice libavfilter libavformat libavutil libswresample libswscale
include $(BUILD_SHARED_LIBRARY)
$(call import-add-path, D:/programming/2021_SUMMER/TimeStampFinder/app)
$(call import-module, libs)