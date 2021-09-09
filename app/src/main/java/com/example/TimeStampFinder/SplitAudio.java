package com.example.TimeStampFinder;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class SplitAudio {

    public int splitWav2Pcm(File wavFile, int sec, Context con){
        FileInputStream in = null;
        FileOutputStream out = null;
        int channels = 1;
        long byteRate = 16 * 16000 * channels/8;
        int splitunit = (int)(sec*byteRate);
        String newFileName = null;
        String newFilePath = null;
        int count = 0;

        try{
            in = new FileInputStream(wavFile);

            byte[] part = new byte[splitunit];
            int avg = (int)(wavFile.length()-44)/splitunit;	// 길이를 정해진 시간단위로 잘라서 몇개인지 알아내기

            int fosize=0;
            in.read(part,0,44); // header(44) 크기만큼 읽기
            while((count++)!=avg) {
                in.read(part,0,splitunit);	// part에 저장
                fosize+=splitunit;
                // file 만들고
                newFileName = (count - 1) +".pcm";
                newFilePath = new FileWrite(newFileName, con).create(true);
                // part 배열을 만든 file에 저장
                out = new FileOutputStream(newFilePath);
                out.write(part);
            }
            // 나머지 데이터(int)(wavFile.length()-44)-fosize
            //int n = in.read(part,0,avg-fosize);
            int remain = in.read(part,0,(int)(wavFile.length()-44)-fosize);
            newFileName = (count - 1) +".pcm";
            newFilePath = new FileWrite(newFileName, con).create(true);
            out = new FileOutputStream(newFilePath);
            out.write(part,0, remain);


            in.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }// FileInputStream.read

        return count;
    }
}
