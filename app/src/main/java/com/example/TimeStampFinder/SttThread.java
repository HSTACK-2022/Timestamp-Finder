package com.example.TimeStampFinder;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.io.File;

public class SttThread extends Thread{

    private int size = 5;   // 저쪽에서 보내는 최대 파일 개수
    private int start;       // 시작 파일 번호
    private int end;         // 끝 파일 번호
    private FileWrite fw;    // 임시 저장을 위한 파일
    private String filePath;    // 파일 경로

    public SttThread(int start, int end, FileWrite fw, String filePath){
        this.start = start-1;
        this.end = end-1;
        this.fw = fw;
        this.filePath = filePath;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void run(){
        System.out.println("START FILE : " + start);
        System.out.println("END FILE : " + end);

        for(int i=start; i<=end; i++){
            String result = new Pcm2Text().pcm2text("korean", "/storage/emulated/0/Music/"+(i+1)+"get.wav");
            fw.write(i+"", filePath);       // index 기록
            fw.write(result, filePath);
        }
        ConvertActivity.setFin((start/size)+1);
    }
}
