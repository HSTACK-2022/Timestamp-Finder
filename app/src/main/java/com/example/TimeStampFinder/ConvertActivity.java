package com.example.TimeStampFinder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static android.content.ContentValues.TAG;
import static java.lang.Thread.sleep;

public class ConvertActivity extends AppCompatActivity {

    private final String TAG = "CONVERT";

    public static Context mContext;
    private VideoView videoView;
    private FrameLayout layout;
    private Fragment wFragment = new TimestampFragment();
    private Fragment iFragment = new StreamFragment();

    private String fileURI;
    private Uri uriFilePath;
    private String txtName;
    private boolean isFull;         // 전체화면 여부를 받기 위한 변수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_convert);

        mContext = this;
        videoView = findViewById(R.id.videoView);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = getIntent();
        fileURI = (String)intent.getSerializableExtra("fileURI");
        uriFilePath = Uri.parse(fileURI);
        txtName = (String)intent.getSerializableExtra("txtName");
        //setResult(RESULT_OK, intent);



        // 방법이 없을까?
        new Thread(){

            public void run(){
                // 비디오를 음원파일로 변경
                String filepath;
                try {
                    Log.d(TAG, Environment.getExternalStorageDirectory().getAbsolutePath());
                    filepath = new File(String.valueOf(fileURI)).getCanonicalPath();



                    //new NDK().scanning(filepath);
                    //new NDK().decode_audio("/storage/emulated/0/Movies/hello.wav", "/storage/emulated/0/Movies/hello.mp3")
                    //new NDK().decode_video("/storage/emulated/0/Movies/videoplayback.mp4", "/storage/emulated/0/Movies/videoplayback.wav");
                    Log.d(TAG, "DECODE_AUDIO : TRUE");
                } catch (IOException e) {
                    Log.e("FFmpegForAndroid", "", e);
                }
            }
        }.start();

        Bundle bundle = new Bundle();
        bundle.putString("fileURI", fileURI);
        bundle.putString("txtName", txtName);
        wFragment.setArguments(bundle);

        isFull = false;
        layout = findViewById(R.id.videoview_frame);

        // 네비게이션 메뉴 활성화
        bottomNavigation();

        // 전체화면 버튼 활성화
        Button fullButton = findViewById(R.id.fullscreen_button);
        fullButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                setFullScreen(!isFull);
                v.setBackgroundResource(isFull ? R.drawable.reduction : R.drawable.fullicon);
            }
        });

        //비디오를 보여주기 시작함
        show(0);

        //영상 길이 알아내기
        Log.d(TAG, "Video Length : "+TimestampFragment.videoLength(fileURI));

    }



    public void show(int n)
    {
        MediaController mc = new MediaController(this); // 비디오 컨트롤 가능하게(일시정지, 재시작 등)
        videoView.setMediaController(mc);
        videoView.setVideoURI(Uri.parse(fileURI));
        videoView.requestFocus();
        videoView.seekTo(n);
        videoView.start();
        //startVideo();
    }

    //네비게이션 메뉴 함수
    void bottomNavigation() {
        //제일 먼저 보여줄 프래그먼트 보여주기
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, wFragment).commitAllowingStateLoss();

        // 바텀 네비게이션 객체 선언
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch(item.getItemId()){
                    case  R.id.tab1:
                        // replace(프레그먼트를 띄워줄 frameLayout, 교체할 fragment 객체)
                        getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, wFragment).commitAllowingStateLoss();
                        return  true;
                    case  R.id.tab2:
                        getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, iFragment).commitAllowingStateLoss();
                        return  true;
                    case  R.id.tab3:
                        Intent intent = new Intent(getBaseContext(), MainActivity.class);
                        startActivityForResult(intent, 1000);
                        return  true;
                    default:
                        return false;
                }
            }
        });
    }

    //비디오가 시작하는 함수
    void startVideo() {
        MediaController mc = new MediaController(this); // 비디오 컨트롤 가능하게(일시정지, 재시작 등)
        videoView.setMediaController(mc);

        videoView.setVideoURI(Uri.parse(fileURI));
        videoView.requestFocus();
        videoView.seekTo(10000);
        videoView.start();
    }

    // 비디오 재시작
    void restartVideo(int sec){
        videoView.seekTo(sec);
    }

    // 비디오 전체화면 설정
    private void setFullScreen(boolean full) {

        isFull = full;
        ViewGroup.LayoutParams params = layout.getLayoutParams();

        if (isFull) {
            isFull = true;
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            params.height = ViewGroup.LayoutParams.MATCH_PARENT;
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            isFull = false;
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int height = (int) (metrics.density * 250);
            params.height = height;

            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        }
    }


}