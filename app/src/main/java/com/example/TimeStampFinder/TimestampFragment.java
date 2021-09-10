package com.example.TimeStampFinder;

import android.content.Context;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;

public class TimestampFragment extends Fragment {

    private final String TAG = "TIMESTAMP_FRAGMENT";

    private RecyclerAdapter adapter;
    private HashMap<String, String> searchRes;

    private String fileURI;
    private String txtName;
    private String txtPath;
    private String suggestion;
    private boolean isFirst = true;
    private static boolean isFin = false;

    private EditText word;
    private ImageButton submit;
    private Switch mode;
    private TextView info;
    private TextView sgWord;
    private ProgressBar progress;

    private Context context;

    private int threadNum = 5;
    private int progValue = 0;

    private ExecutorService split = null;
    private ExecutorService manage = null;
    private ExecutorService pool = null;

    //video 이름과 같은 wav 파일 이름 및 경로 알아내기 (파일 생성 X)
    private String getAudioFilePath(String fileName){

        int cut = fileName.lastIndexOf('/');
        if (cut != -1) {
            fileName = fileName.substring(cut + 1);
        }
        fileName = fileName.substring(0,fileName.length()-4);
        String audio_name = fileName+".wav";
        String audio_path = context.getFilesDir()+"/"+audio_name;
        File wav = new File(audio_path);

        if(wav.exists())   wav.delete();
        return audio_path;
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

        // view 설정
        word = view.findViewById(R.id.searchText);
        submit = view.findViewById(R.id.imageButton);
        mode = view.findViewById(R.id.switchMode);
        sgWord = view.findViewById(R.id.suggestion);
        info = view.findViewById(R.id.infoTextView);
        progress = view.findViewById(R.id.progressBar);

        adapter = new RecyclerAdapter();
        recyclerView.setAdapter(adapter);

        if(isFirst){
            mode.setEnabled(false);
            submit.setEnabled(false);
            sgWord.setText("파일 읽는 중...");
            progValue = 0;
            progress.setProgress(0);
        }
        else{
            sgWord.setText(suggestion);
            info.setText("를 검색해보세요!");
        }

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        context = getActivity().getApplicationContext();

        if(isFirst){
            // 오디오 분할 시작 -> 이후 멀티스레드로 텍스트 읽기 -> 텍스트 파일 합치기 자동 진행
            split = Executors.newSingleThreadExecutor();
            new SplitAsync().executeOnExecutor(split);
            split.shutdown();
        }
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
                        String timeStamp = Integer.parseInt(listTitle.get(i))/6+":"+Integer.parseInt(listTitle.get(i))%6*10;
                        data.setTitle(timeStamp);
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

    @Override
    public void onPause() {
        super.onPause();
        try {
            boolean check = split.awaitTermination(30, TimeUnit.SECONDS);
            if(manage!=null)    manage.awaitTermination(0, TimeUnit.SECONDS);
            if(pool!=null)      pool.awaitTermination(0, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 파일 분할을 위한 Async
    public class SplitAsync extends AsyncTask<Object, Integer, Integer>{

        @Override
        protected Integer doInBackground(Object... objects) {
            int audioNum;
            String audiopath = getAudioFilePath(fileURI);       // audioPath = fileURI(mp4의)의 경로
            
            //extract Audio Stream(wav < 16bit 16kHz mono >) from Video(mp4)
            new AudioExtractor().useFfmpeg(fileURI, audiopath); // mp4경로, wav경로

            // wav -> 10sec씩 pcm 파일로 쪼개기 (파일명은 0.pcm, 1.pcm, ...)
            File wav = new File(audiopath);
            audioNum = new SplitAudio().splitWav2Pcm(wav,10, context);

            progValue += 10;
            return audioNum;
        }

        @Override
        protected void onProgressUpdate(Integer... integers){
            progress.setProgress(progValue);
        }

        // audioNum이 넘어오면 스레드를 분할해 텍스트 읽기 시작
        @Override
        protected void onPostExecute(Integer audioNum) {
            int asyncNum = audioNum/threadNum;                          // 한 스레드당 보낼 오디오 파일의 수
            SttAsync[] stt = new SttAsync[threadNum];                   // 각 스레드를 담을 배열
            String[] tempFilePath = new String[threadNum];              // 스레드당 임시 파일의 경로를 담을 배열 (temp0.txt ...)
            manage = Executors.newSingleThreadExecutor();   // 이후 파일 통합시 사용할 스레드 풀
            pool = Executors.newFixedThreadPool(threadNum); // 멀티스레드를 병렬 실행, 관리할 스레드 풀

            FileWrite fw = new FileWrite(txtName, context);         // 통합 txt
            txtPath = fw.create(false);

            // 각 스레드에 대해
            for(int i = 0; i<threadNum; i++){
                FileWrite temp = new FileWrite("temp"+i+".txt", context);
                tempFilePath[i] = temp.create(true);

                int audioStart = i*asyncNum;
                int audioEnd = (i==threadNum-1)?audioNum-1:(i+1)*asyncNum-1;        // 오디오 파일의 끝이면 마지막 번호 return

                stt[i] = new SttAsync(i, audioStart, audioEnd, temp, tempFilePath[i]);
                stt[i].executeOnExecutor(pool);
            }
            pool.shutdown();

            // 통합 파일로 저장
            new SttManage().executeOnExecutor(manage, threadNum, fw, tempFilePath, pool);
            manage.shutdown();
        }
    }

    // 파일을 분할해 각 스레드로 보내는 SttAsync
    public class SttAsync extends AsyncTask<Object, Integer, Integer>{

        private final String TAG = "STTASYNC";

        private int keyNum;
        private int startNum;
        private int endNum;
        private FileWrite fw;
        private final String filePath;
        private String audioPath = context.getCacheDir()+"/";

        String[] keys = {"2d40b072-37f1-4317-9899-33e0b3f5fb90","80ff5736-f813-4686-aca6-472739d8ebe0","25833dd1-e685-4f13-adc6-c85341d1bac5",
                "40c498a8-7d33-4909-9b60-427b3d0ccf8b", "0913ccd7-0cd1-4455-8b60-7940aa54f7be"};

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
                // num %2d로 처리
                String numStr = Integer.toString(i);
                String content = new Pcm2Text().pcm2text(audioPath+i+".pcm", keys[keyNum]);
                // 단어와 Content 함께 기록
                if(i==startNum) fw.write(numStr+"\n"+content, filePath, true);
                else            fw.write(numStr+"\n"+content, filePath, false);
                progValue+=1;
                publishProgress();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... integers){
            progress.setProgress(progValue);
        }
    }

    // 여러개의 SttAsync을 돌린 뒤 취합하는 스레드
    public class SttManage extends AsyncTask<Object, Integer, Void>{

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected Void doInBackground(Object... objects){
            int threadNum = (int)objects[0];
            FileWrite fw = (FileWrite)objects[1];
            String tempFilePath[] = (String[])objects[2];
            ExecutorService pool = (ExecutorService)objects[3];

            // 스레드 작업이 모두 끝나면
            try{
                boolean check = pool.awaitTermination(300, TimeUnit.SECONDS);
                if(check){
                    progValue+=10;
                    publishProgress();

                    for(int i = 0; i<threadNum; i++){
                        String str = FileWrite.read(tempFilePath[i]);
                        Log.d(TAG, tempFilePath[i]+": "+str);
                        fw.write(str, txtPath, true);
                        progValue+=1;
                        publishProgress();
                    }
                }
            }
            catch(Exception e){
                Log.e(TAG, e+"");
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... integers){
            progress.setProgress(progValue);
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected void onPostExecute(Void v){
            new Thread(){
                @Override
                public void run(){
                    try {
                        isFirst = false;
                        suggestion = SuggestWord.suggest(txtPath);
                        progress.setProgress(progValue = 100);
                        sgWord.setText(suggestion);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.start();
            info.setText("를 검색해보세요!");
            mode.setEnabled(true);
            submit.setEnabled(true);
        }
    }
}