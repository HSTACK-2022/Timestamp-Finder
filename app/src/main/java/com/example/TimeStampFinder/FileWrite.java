package com.example.TimeStampFinder;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;

public class FileWrite {

    private final String TAG = "FILE WRITE";
    private final String fileName;
    private final Context con;

    public FileWrite(String fileName, Context con){
        this.fileName = fileName;
        this.con = con;
    }

    public String write(String content){
        FileWriter writer;
        try{
            File path = con.getFilesDir();
            File dir = new File(path, fileName);
            if(!dir.exists())
                dir.mkdir();

            Log.d(TAG, "FILE PATH : "+path);

            File file = new File(dir+"/"+fileName);
            if(!file.exists())
                file.createNewFile();

            writer = new FileWriter(file, true);
            writer.write("\n"+content);
            writer.flush();
            writer.close();
            return dir+"/"+fileName;
        } catch(Exception e) {
            Log.d(TAG, "ERROR : " + e);
            e.printStackTrace();
            return "ERROR";
        }
    }
}
