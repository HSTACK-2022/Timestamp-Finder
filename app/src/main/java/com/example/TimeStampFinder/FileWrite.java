package com.example.TimeStampFinder;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FileWrite {

    private final String TAG = "FILE WRITE";
    private final String fileName;
    private final Context con;
    private File file;

    public FileWrite(String fileName, Context con){
        this.fileName = fileName;
        this.con = con;
    }

    // 빈 파일을 하나 만들어 경로를 리턴하는 함수
    public String create() {
        try {
            File path = con.getFilesDir();
            File dir = new File(path, fileName);
            if (!dir.exists())
                dir.mkdir();

            Log.d(TAG, "FILE PATH : " + path);

            file = new File(dir + "/" + fileName);
            if (!file.exists())
                file.createNewFile();

            Log.d(TAG, "return message : "+dir+"/"+fileName);
            return dir+"/"+fileName;
        }
        catch(Exception e) {
            Log.d(TAG, "ERROR : " + e);
            e.printStackTrace();
            return "ERROR";
        }
    }

    // path의 파일에 내용을 쓰는 함수
    public void write(String content, String path) {
        FileWriter writer;
        File file = new File(path);
        try {
            writer = new FileWriter(file, true);
            writer.write("\n" + content);
            writer.flush();
            writer.close();
        }
        catch (IOException e) {
            Log.d(TAG, "ERROR : " + e);
            e.printStackTrace();
        }
    }

    public static String read(String filePath){
        File file = new File(filePath) ;
        FileReader fr = null ;
        char[] cbuf = new char[512] ;
        String text = "";
        int size = 0 ;

        try {
            // open file.
            fr = new FileReader(file) ;
            // read file.
            while ((size = fr.read(cbuf)) != -1) {
                // TODO : use data
                System.out.println("read size : " + size) ;
                for (int i=0; i<size; i++) {
                    text+=cbuf[i];
                }
            }
            text+="\n";
            fr.close();
            return text;
        } catch (Exception e) {
            e.printStackTrace();
            return "READ ERROR";
        }
    }
}
