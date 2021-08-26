package com.example.TimeStampFinder;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;

import java.io.IOException;

import static com.example.TimeStampFinder.Uri2Path.getPath;


public class MainActivity extends AppCompatActivity {

    private static final int REQ_CODE = 123;
    private static final String TAG = "MAIN";
    private String fileURI;         // 불러올 영상의 Uri
    private String txtName;         // text file은 영상당 하나로 제한
    private String txtPath;         // text file에 저장 뒤 경로 전달


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 시작하자마자 사용자로부터 권한 확인
        checkPermission();

        // button을 클릭하면 파일을 불러옴.
        Button button = findViewById(R.id.button);
        button.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 지금은 */*으로 모든 파일을 허용했으나, 후에 audio/x-wav로 wav파일로 제한 필요
                Intent intent = new Intent().setType("*/*").setAction(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(Intent.createChooser(intent, "Select a file"), REQ_CODE);
            }
        }));
    }

    // file access permission from user.
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void checkPermission() {
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    // onActivityResult()에서 결과값 처리
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Context context = getApplicationContext();

        if (requestCode == REQ_CODE && resultCode == RESULT_OK) {
            Uri selectedfile = data.getData(); //The uri with the location of the file

            // 경로 정보:  selectedfile.getPath()
            // 전체 URI 정보: selectedfile.toString()
            Toast.makeText(getApplicationContext(), getFileNameFromUri(selectedfile) + "을 불러옵니다.", Toast.LENGTH_LONG).show();
            fileURI = getPath(this, selectedfile);
            Log.d(TAG, "fileURI : " + fileURI);

            // 사용자의 file을 바탕으로 textfile의 fileName 설정
            String[] getTxtName = getFileNameFromUri(selectedfile).split(".wav");
            txtName = getTxtName[0] + ".txt";
            Log.d(TAG, "FILE SPLIT : " + txtName);

            //파일이 선택되면 두번째 activity로 넘기기(txt파일 생성)
            Intent intent = new Intent(getBaseContext(), convertActivity.class);
            intent.putExtra("fileURI",fileURI);
            intent.putExtra("txtName", txtName);
            startActivityForResult(intent, 1000);

        }
    }

    // URI에서 파일명 얻기
    private String getFileNameFromUri(Uri uri) {
        String fileName = "";

        Cursor cursor = getContentResolver().query(uri, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
        }
        cursor.close();

        return fileName;
    }
}