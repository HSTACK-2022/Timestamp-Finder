package com.example.TimeStampFinder;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class MediaActivity extends AppCompatActivity {

    private static final String TAG = "MEDIA";
    private String fileURI;         // 불러올 영상의 Uri
    private String txtName;         // text file은 영상당 하나로 제한
    private String txtPath;         // text file에 저장 뒤 경로 전달

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media);

        // intent 빼오기
        Intent intent = getIntent();
        fileURI = intent.getStringExtra("fileURI");
        txtName = intent.getStringExtra("txtName");

    }
}
