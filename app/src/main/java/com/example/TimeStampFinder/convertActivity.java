package com.example.TimeStampFinder;

import android.content.Intent;
import android.content.pm.ActivityInfo;
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

import static android.content.ContentValues.TAG;

public class convertActivity extends AppCompatActivity {
    VideoView videoView;
    boolean isFull;
    FrameLayout layout;
    Fragment wFragment = new timestampFragment();
    Fragment iFragment = new streamFragment();

    private String fileURI;
    private String txtName;
    private String txtPath;
    private FileWrite fw;           // 변환된 텍스트 파일을 생성하고 경로를 받기 위한 객체
    private boolean isFinished = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_convert);

        videoView = findViewById(R.id.videoView);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = getIntent();
        String str = intent.getStringExtra("uri");
        //setResult(RESULT_OK, intent);
        Log.d(TAG, "RESULT str:" + str);

        // intent에서 파일 경로 빼내기
        fileURI = (String)intent.getSerializableExtra("fileURI");
        txtName = (String)intent.getSerializableExtra("txtName");

        // 빈 파일 생성, 경로만 미리 가져오기
        fw = new FileWrite(txtName, getApplicationContext());
        txtPath = fw.create();

        new Thread() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            public void run() {
                // 지금은 언어를 고정해두지만 후에는 사용자가 선택할 수 있게끔 해야함.
                String result = new Pcm2Text().pcm2text("korean", fileURI);
                fw.write(result, txtPath);
                timestampFragment.setfinish(true);      // Fragment에 변수 변경 알려주기
            }
        }.start();

        Bundle bundle = new Bundle();
        bundle.putString("uri",str);
        bundle.putString("txtPath", txtPath);
        wFragment.setArguments(bundle);

        isFull = false;
        layout = findViewById(R.id.videoview_frame);

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

        Button button = findViewById(R.id.fullscreen_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFullScreen(!isFull);
                v.setBackgroundResource(isFull ? R.drawable.reduction : R.drawable.fullicon);
            }
        });

//        Bundle bundle = new Bundle();
//        bundle.putString("uri",str);
//
//        stFragment.setArguments(bundle);


        MediaController mc = new MediaController(this); // 비디오 컨트롤 가능하게(일시정지, 재시작 등)
        videoView.setMediaController(mc);

        videoView.setVideoURI(Uri.parse(str));
        videoView.requestFocus();
        videoView.start();

        //영상 길이 알아내기
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(str);

        String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        long timeInmillisec = Long.parseLong(time); //예시로 7531 이면

//        Intent rIntent = new Intent(convertActivity.this, RecyclerAdapter.class);
//        rIntent.putExtra("time",timeInmillisec);
//        startActivity(rIntent);

        long duration = timeInmillisec / 1000; // 7.531 초
        long hours = duration / 3600;
        //long hours = TimeUnit.MILLISECONDS.toHours(timeInmillisec); 위랑 동일. TimeUnit 함수 쓴 것 뿐
        long minutes = (duration - hours * 3600) / 60; // 1분에 60000 msec임
        long seconds = duration - (hours * 3600 + minutes * 60);
        System.out.println("TIME length"+ time +"duration : "+ duration +" hours: " + hours + ", minutes: " + minutes + ", seconds: " + seconds);


        //버튼 누르면 해당 시간으로 가는 함수
//        Button tbutton = findViewById(R.id.timebtn);
//        tbutton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                videoView.seekTo((int) timeInmillisec/2);
//                videoView.start();
//                System.out.println("hours: " + hours + ", minutes: " + minutes + ", seconds: " + seconds);
////                System.out.format("%02d:%02d:%02d", hours, minutes, seconds);
//            }
//        });


    }


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
