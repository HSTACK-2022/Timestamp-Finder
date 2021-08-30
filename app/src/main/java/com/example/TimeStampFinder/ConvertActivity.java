package com.example.TimeStampFinder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
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
import android.widget.ProgressBar;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static android.content.ContentValues.TAG;
import static java.lang.Thread.sleep;

public class ConvertActivity extends AppCompatActivity {

    public static Context mContext;
    private VideoView videoView;
    private FrameLayout layout;
    private Fragment wFragment = new TimestampFragment();
    private Fragment iFragment = new StreamFragment();

    private String fileURI;
    private String txtPath;
    private String txtName;
    private boolean isFull;         // 전체화면 여부를 받기 위한 변수

    private int fileNum = 12;
    private FileWrite fw;           // 변환된 텍스트 파일을 생성하고 경로를 받기 위한 객체
    private SttManager sttManager = new SttManager();

    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_convert);

        mContext = this;
        videoView = findViewById(R.id.videoView);
        progress = findViewById(R.id.progressBar);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = getIntent();
        fileURI = (String)intent.getSerializableExtra("fileURI");
        txtName = (String)intent.getSerializableExtra("txtName");
        //setResult(RESULT_OK, intent);

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

        // STT
        // 빈 파일 생성, 경로만 미리 가져오기
        fw = new FileWrite(txtName, getApplicationContext());
        txtPath = fw.create();
        sttManager.execute(fileNum, fw, txtPath, getApplicationContext());

        Bundle bundle = new Bundle();
        bundle.putString("fileURI", fileURI);
        bundle.putString("txtName", txtName);
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

    // SttManager
    class SttManager extends AsyncTask<Object, Integer, Boolean> {

        private final String TAG = "SttManager";

        private int size = 5;   // 최대로 만들 수 있는 스레드의 수
        private int progGap = 100/size;     // 1개의 스레드당 할당된 progressbar의 크기
        private int progStatus = 0;         // progressbar의 진행 현황
        private String keys[] = {"2d40b072-37f1-4317-9899-33e0b3f5fb90",};

        @SuppressLint("WrongThread")
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        protected Boolean doInBackground(Object... objects) {

            // 들어온 총 파일의 수를 5개로 분할하고, 각각 5개의 AsyncTask로 전달
            int i;
            int asyncNum = (int)objects[0]/size;        // 1개의 스레드에 보낼 최대 파일의 수
            FileWrite fw = (FileWrite)objects[1];       // 파일 작성에 필요한 FileWriter
            String txtPath = (String)objects[2];        // 모든 파일을 취합해 만들 최종 텍스트 파일의 경로
            Context context = (Context)objects[3];      // fileWrite를 위한 context

            String tempPath[] = new String[size];       // fileWrite로 생성된 임시 파일의 경로 저장

            ExecutorService threadPool = Executors.newFixedThreadPool(size);        // 스레드를 관리하기 위한 pool
            SttAsynctask sat[] = new SttAsynctask[size];                            // 나중에 여부 알려면 스레드 모아둬야함

            for(i=0;i<size;i++){
                int startIndex = i*asyncNum;
                int endIndex;

                // 오디오 시작, 종료 index 지정
                if(i==size-1)   endIndex = (int)objects[0]-1;
                else            endIndex = startIndex+asyncNum-1;

                FileWrite temp = new FileWrite("temp"+i+".txt", context);       // 각 스레드별 임시 파일 생성
                tempPath[i] = temp.create();
                new SttAsynctask().executeOnExecutor(threadPool, startIndex, endIndex, temp, tempPath[i], keys[0]);
            }
            Log.d(TAG, "ESCAPED");

            while(i<size){
                try{sleep(1000);}
                catch(Exception e){e.printStackTrace();}
                Log.d(TAG, sat[i].getStatus().toString());
            }

            // check Fin
            while(i<size){
                if(sat[i].getStatus()==Status.FINISHED)
                    i++;
                try{sleep(1000);}
                catch(Exception e){e.printStackTrace();}
                Log.d(TAG, sat[i].getStatus().toString());
            }

            // 빠져나오면 모든 스레드에서 API가 돌았다는 뜻
            // 파일 합산 시작
            for(i=0; i<size; i++){
                fw.write(FileWrite.read(tempPath[i]), txtPath);
            }

            return true;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            // 파일 다운로드 퍼센티지 표시 작업
        }

        @Override
        protected void onPostExecute(Boolean b) {
            TimestampFragment.setFin();
            Log.d(TAG, b.toString());
        }


        // API로 직접적으로 보내는 스레드
        class SttAsynctask extends AsyncTask<Object, Integer, Integer>{
            private int size = 5;   // 저쪽에서 보내는 최대 파일 개수
            private int start;       // 시작 파일 번호
            private int end;         // 끝 파일 번호
            private FileWrite fw;    // 임시 저장을 위한 파일
            private String filePath;    // 파일 경로
            private String key;         // API Access Key

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            protected Integer doInBackground(Object[] objects) {
                int start = (int)objects[0];
                int end = (int)objects[1];
                FileWrite fw = (FileWrite)objects[2];
                String filePath = (String)objects[3];
                String key = (String)objects[4];

                //progressbar 작업
                int index = start/size;                 // start로 내가 몇번째 스레드인지 알아내기
                int threadGap = progGap/(end-start);    // 하나의 API가 호출될 때 마다 이동할 progressbar의 크기

                for(int i=start; i<=end; i++){
                    String result = new Pcm2Text().pcm2text("korean", "/storage/emulated/0/Music/"+(i+1)+"get.wav", key);
                    fw.write(i+"", filePath);       // index 기록
                    fw.write(result, filePath);
                    progStatus+=threadGap;
                }
                Log.d(TAG, "isFinished");
                return progStatus;
            }

            protected void onProgressUpdate(Integer ... values) {
                progress.setProgress(values[0].intValue());
                Log.i(TAG, "현재 진행 값 : " + values[0].toString());
            }

            //이 Task에서(즉 이 스레드에서) 수행되던 작업이 종료되었을 때 호출됨
            protected void onPostExecute(Integer result) {
                progress.setProgress(progStatus);
                Log.i(TAG, "완료되었습니다");
            }
        }
    }
}