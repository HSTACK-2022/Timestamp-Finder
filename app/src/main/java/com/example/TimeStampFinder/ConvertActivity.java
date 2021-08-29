package com.example.TimeStampFinder;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
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

import static android.content.ContentValues.TAG;
import static java.lang.Thread.sleep;

public class ConvertActivity extends AppCompatActivity {

    public static Context mContext;
    private VideoView videoView;
    private FrameLayout layout;
    private Fragment wFragment = new TimestampFragment();
    private Fragment iFragment = new StreamFragment();

    private String fileURI;
    private String txtName;
    private String txtPath;
    private FileWrite fw;           // 변환된 텍스트 파일을 생성하고 경로를 받기 위한 객체
    private boolean isFull;         // 전체화면 여부를 받기 위한 변수
    private static boolean isFin[]; // STT가 모두 돌아갔는지 확인하기 위한 변수

    // 각 스레드로 분산하여 보낸 파일들이 잘 들어갔는지 확인하기 위한 함수
    // nget.wav부터 시작한 파일의 기록 여부는 inFin[n]에 담겨있음
    public static void setFin(int index){ isFin[index] = true; }

    // 파일이 모두 다 기록되었는지 확인하기 위한 함수
    private boolean finCheck(){
        for(int i=1;i<isFin.length;i++){
            if(!isFin[i])
                return false;
        }
        return true;
    }

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
        txtName = (String)intent.getSerializableExtra("txtName");
        //setResult(RESULT_OK, intent);

        // 빈 파일 생성, 경로만 미리 가져오기
        fw = new FileWrite(txtName, getApplicationContext());
        txtPath = fw.create();

        // 방법이 없을까?
        new Thread(){
            public void run(){
                // 비디오를 음원파일로 변경
                String filepath;
                try {
                    Log.d(TAG, Environment.getExternalStorageDirectory().getAbsolutePath());
                    filepath = new File(fileURI).getCanonicalPath();
                    new NDK().scanning(filepath);
                    //new NDK().decode_audio("/storage/emulated/0/Movies/hello.wav", "/storage/emulated/0/Movies/hello.mp3")
                    //new NDK().decode_video("/storage/emulated/0/Movies/videoplayback.mp4", "/storage/emulated/0/Movies/videoplayback.wav");
                    Log.d(TAG, "DECODE_AUDIO : TRUE");
                } catch (IOException e) {
                    Log.e("FFmpegForAndroid", "", e);
                }
            }
        }.start();

        Bundle bundle = new Bundle();
        bundle.putString("fileURI",fileURI);
        bundle.putString("txtPath", txtPath);
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

        // 잘린 wav 파일이 모두 들어왔다고 가정하고 스레드로 넘기기
        int size = 12;      // 파일의 개수를 임의로 지정하자.

        new Thread(){
            public void run(){
                int maxFile = 5;       // 한번에 보낼 파일의 수
                int num = (size/maxFile)+1;   // 5개씩 나눠 보낼 예정
                isFin = new boolean[num+1];
                String tmpPath[] = new String[num+1];

                for(int i=1;i<=num;i++){
                    //isFin 초기화
                    isFin[i] = false;
                    // 새로운 FileWriter 생성
                    String tmpName = "temp"+i+".txt";
                    FileWrite fw = new FileWrite(tmpName, getApplicationContext());
                    tmpPath[i] = fw.create();
                    // 새로운 스레드 생성 : 5개의 오디오파일을 기록하는 스레드
                    SttThread st;
                    if(i==num)      st = new SttThread((i-1)*maxFile+1, size, fw, tmpPath[i]);
                    else            st = new SttThread((i-1)*maxFile+1, i*maxFile, fw, tmpPath[i]);
                    st.run();
                }

                do{
                    try{sleep(1000);}
                    catch(Exception e){ e.printStackTrace(); }
                }while(!finCheck());

                // 파일 합산
                for(int i=1;i<=num;i++){
                    fw.write(FileWrite.read(tmpPath[i]), txtPath);
                }

                // 다 끝났으면 TimestampFragment로 알림
                TimestampFragment.setfinish(true);
            }
        }.start();

        /*
        new Thread() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            public void run() {
                // 지금은 언어를 고정해두지만 후에는 사용자가 선택할 수 있게끔 해야함.
                String result = new Pcm2Text().pcm2text("korean", fileURI);
                fw.write(result, txtPath);
                TimestampFragment.setfinish(true);      // Fragment에 변수 변경 알려주기
            }
        }.start();
         */
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