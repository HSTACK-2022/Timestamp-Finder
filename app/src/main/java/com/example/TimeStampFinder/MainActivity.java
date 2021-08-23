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


            new Thread() {
                public void run() {
                    // 지금은 언어를 고정해두지만 후에는 사용자가 선택할 수 있게끔 해야함.
                    String result = new Pcm2Text().pcm2text("korean", fileURI);
                    txtPath = new FileWrite(txtName, getApplicationContext()).write(result);
                }
            }.start();

            //파일이 선택되면 두번째 activity로 넘기기(txt파일 생성)
            Intent intent = new Intent(getBaseContext(), convertActivity.class);
            intent.putExtra("uri",fileURI);
            intent.putExtra("txtName", txtName);
            intent.putExtra("txtPath", txtPath);
            startActivityForResult(intent, 1000);
        }

        //check
        String filepath;
        try {
            Log.d(TAG, Environment.getExternalStorageDirectory().getAbsolutePath());
            filepath = new File(fileURI).getCanonicalPath();
            new NDK().scanning(filepath);
        } catch (IOException e) {
            Log.e("FFmpegForAndroid", "", e);
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

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri     The Uri to query.
     * @author paulburke
     */
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                Log.d(TAG, "Type : document");
                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    Log.d(TAG, "Type : " + type);
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
}