package com.example.TimeStampFinder;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import static java.lang.Thread.sleep;

public class TimestampFragment extends Fragment {

    private final String TAG = "TIMESTAMP_FRAGMENT";

    private RecyclerAdapter adapter;
    private HashMap<String, String> searchRes;

    private String fileURI;
    private String txtName;
    private String txtPath;
    private static boolean isFin = false;

    private EditText word;
    private ImageButton submit;
    private Switch mode;
    private TextView sgWord;
    private TextView info;
    private ProgressBar progress;

    private Context context;

    private int threadNum = 5;
    private int progValue = 0;

    // txtFile의 완성 여부 확인
    public static void setFin(){ isFin = true; }

    // 영상 길이를 확인하기 위한 함수
    public static String videoLength(String fileURI){
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(fileURI);

        String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        long timeInmillisec = Long.parseLong(time); //예시로 7531 이면
        long duration = timeInmillisec / 1000; // 7.531 초
        long hours = duration / 3600;
        //long hours = TimeUnit.MILLISECONDS.toHours(timeInmillisec); 위랑 동일. TimeUnit 함수 쓴 것 뿐
        long minutes = (duration - hours * 3600) / 60; // 1분에 60000 msec임
        long seconds = duration - (hours * 3600 + minutes * 60);

        String result = "TIME length"+ time +"duration : "+ duration +" hours: " + hours + ", minutes: " + minutes + ", seconds: " + seconds;
        return result;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Bundle 빼내기
        Bundle bundle = getArguments();
        fileURI = bundle.getString("fileURI");
        txtName = bundle.getString("txtName");

        Log.d(TAG, "RESULT frag : " + fileURI);
        Log.d(TAG, "TXT NAME " + txtName);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_timestamp,container,false);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView); //얘는 view대신 getView를 써야한다 이유는 위에서 return 했기 때문
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        //영상 길이 알아내기
        Log.d(TAG, "Video Length : "+TimestampFragment.videoLength(fileURI));

        // view 설정
        word = view.findViewById(R.id.searchText);
        submit = view.findViewById(R.id.imageButton);
        mode = view.findViewById(R.id.switchMode);
        sgWord = view.findViewById(R.id.suggestion);
        info = view.findViewById(R.id.infoTextView);
        progress = view.findViewById(R.id.progressBar);

        adapter = new RecyclerAdapter();
        recyclerView.setAdapter(adapter);

        submit.setEnabled(false);
        sgWord.setText("파일 읽는 중...");

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        context = getActivity().getApplicationContext();

        // 파일 분할이 들어올 자리
        int audioNum = 12;

        // STT
        // 빈 파일 생성, 경로만 미리 가져오기
        int i;
        int asyncNum = audioNum/threadNum;
        SttAsync[] stt = new SttAsync[threadNum];
        String[] tempFilePath = new String[threadNum];
        ExecutorService manage = Executors.newSingleThreadExecutor();
        ExecutorService pool = Executors.newFixedThreadPool(threadNum);
        
        FileWrite fw = new FileWrite(txtName, context);         // 통합 txt
        txtPath = fw.create();

        // 각 스레드에 대해
        for(i = 0; i<threadNum; i++){
            FileWrite temp = new FileWrite(i+"temp.txt", context);
            tempFilePath[i] = temp.create();

            int audioStart = i*asyncNum;
            int audioEnd = (i==threadNum-1)?audioNum-1:(i+1)*asyncNum-1;

            stt[i] = new SttAsync(i,audioStart, audioEnd, temp, tempFilePath[i]);
            stt[i].executeOnExecutor(pool);
        }
        pool.shutdown();
        
        // 통합 파일로 저장
        new SttManage().executeOnExecutor(manage, threadNum, fw, tempFilePath, pool);
        manage.shutdown();

        // suggest 구현
        new Thread() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            public void run() {

            }
        }.start();

        // search 구현
        // submit 이미지 버튼을 클릭하면 검색이 시작된다.
        submit.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.removeAll();
                SearchWord sw = new SearchWord(word.getText().toString(), txtPath, mode.isChecked());
                try{
                    searchRes = sw.findWord();
                    List<String> listTitle = new ArrayList<>(searchRes.keySet());
                    List<String> listContent = new ArrayList<>(searchRes.values());

                    for (int i = 0; i < listTitle.size(); i++) {
                        // 각 List의 값들을 data 객체에 set 해줍니다.
                        Data data = new Data();
                        data.setTitle(listTitle.get(i));
                        data.setContent(listContent.get(i));

                        // 각 값이 들어간 data를 adapter에 추가합니다.
                        adapter.addItem(data);
                    }

                    // adapter의 값이 변경되었다는 것을 알려줍니다.
                    adapter.notifyDataSetChanged();

                }
                catch(Exception e){
                    e.printStackTrace();
                    Log.e(TAG, " ", e);
                }
            }
        }));
    }

    public class SttAsync extends AsyncTask<Object, Integer, Integer>{

        private final String TAG = "STTASYNC";

        private int keyNum;
        private int startNum;
        private int endNum;
        private FileWrite fw;
        private String filePath;

        private String audioPath = "/storage/emulated/0/Music/";

        String keys[] = {"2d40b072-37f1-4317-9899-33e0b3f5fb90"};

        public SttAsync(int keyNum, int startNum, int endNum, FileWrite fw, String filePath){
            this.startNum = startNum;
            this.endNum = endNum;
            this.fw = fw;
            this.filePath = filePath;
            this.keyNum = keyNum;
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        protected Integer doInBackground(Object... objects) {
            progValue+=5;
            publishProgress();
            for (int i=startNum; i<=endNum; i++){
                String content = new Pcm2Text().pcm2text("korean", audioPath+i+"get.wav", keys[0]);
                if(i==startNum)
                    fw.write(i+"\n"+content, filePath, true);
                else
                    fw.write(i+"\n"+content, filePath, false);
                progValue+=5;
                publishProgress();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... integers){
            progress.setProgress(progValue);
        }
    }

    public class SttManage extends AsyncTask<Object, Integer, Integer>{

        @Override
        protected Integer doInBackground(Object... objects){
            int threadNum = (int)objects[0];
            FileWrite fw = (FileWrite)objects[1];
            String tempFilePath[] = (String[])objects[2];
            ExecutorService pool = (ExecutorService)objects[3];

            // 스레드 작업이 모두 끝나면
            do{}while(!pool.isTerminated());
            progValue+=25;
            publishProgress();

            for(int i = 0; i<threadNum; i++){
                String str = FileWrite.read(tempFilePath[i]);
                Log.d(TAG, str);
                fw.write(str, txtPath, true);
                progValue+=5;
                publishProgress();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... integers){
            progress.setProgress(progValue);
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected void onPostExecute(Integer result){
            // suggest
            try {
                sgWord.setText(SuggestWord.suggest(txtPath));
                info.setText("를 검색해보세요!");
            } catch (Exception e) {
                e.printStackTrace();
            }

            submit.setEnabled(true);
        }
    }
}