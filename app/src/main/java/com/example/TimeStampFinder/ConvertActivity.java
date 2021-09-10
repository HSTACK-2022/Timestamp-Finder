package com.example.TimeStampFinder;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaExtractor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;
import java.io.IOException;

public class ConvertActivity extends AppCompatActivity {

    int n = 0;
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
    private String originHeight;
    private String originWidth;
    private MediaMetadataRetriever retriever;

    // 영상 길이를 확인하기 위한 함수
    public static int videoLength(String fileURI){
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(fileURI);

        String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        long timeInmillisec = Long.parseLong(time); //예시로 7531 이면
        long duration = timeInmillisec / 1000; // 7.531 초
        return (int)duration;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_convert);

        mContext = this;
        videoView = findViewById(R.id.videoView);
        retriever = new MediaMetadataRetriever();
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

        Bundle bundle = new Bundle();
        bundle.putString("fileURI", fileURI);
        bundle.putString("txtName", txtName);
        bundle.putInt("fileLength", videoLength(fileURI));
        wFragment.setArguments(bundle);
        iFragment.setArguments(bundle);

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
        Log.d(TAG, "Video Length : " + videoLength(fileURI)+"sec.");
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

        // 비디오 파일의 가로세로 길이
        retriever.setDataSource(this, Uri.parse(fileURI));
        String originWidth = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
        String originHeight = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);

        MediaExtractor mediaExtractor = new MediaExtractor();
        try {
            mediaExtractor.setDataSource(fileURI);

        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d("프레임 길이: ", Integer.toString(n));
        Log.d("메타데이터 길이 :", originWidth + " : " + originHeight);

        if (isFull) {
            isFull = true;
            if (Integer.parseInt(originHeight) < Integer.parseInt(originWidth))
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            else
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            params.height = ViewGroup.LayoutParams.MATCH_PARENT;

            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        } else {
            isFull = false;
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int height = (int) (metrics.density * 250);
            params.height = height;
            //Log.d("videoview SIZE:", "x: " + videoView.getPivotX() + " y: " + videoView.getPivotY());
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }
}