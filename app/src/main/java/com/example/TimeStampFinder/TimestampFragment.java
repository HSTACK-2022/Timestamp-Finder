package com.example.TimeStampFinder;

import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static android.content.ContentValues.TAG;
import static java.lang.Thread.sleep;

public class TimestampFragment extends Fragment {

    private final String TAG = "TIMESTAMP_FRAGMENT";

    VideoView tvideoView;
    private RecyclerAdapter adapter;
    private HashMap<String, String> searchRes;
    private static Boolean isFin = false;

    // 음성인식 스레드가 끝났는지 확인하기 위한 함수
    public static void setfinish(boolean b){ isFin = b; Log.d("TIMESTAMP", "true");}

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
        }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_timestamp,container,false);
        //tvideoView = (VideoView)view.findViewById(R.id.videoView);

        Bundle bundle = getArguments();
        String fileURI = bundle.getString("fileURI");
        String txtPath = bundle.getString("txtPath");

        Log.d(TAG, "RESULT frag : " + fileURI);
        Log.d(TAG, "Text Path " + txtPath);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView); //얘는 view대신 getView를 써야한다 이유는 위에서 return 했기 때문
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        adapter = new RecyclerAdapter();
        recyclerView.setAdapter(adapter);

        //영상 길이 알아내기
        Log.d(TAG, "Video Length : "+TimestampFragment.videoLength(fileURI));

        // 검색 기능 구현을 위한 변수 선언
        EditText word = view.findViewById(R.id.searchText);
        ImageButton submit = view.findViewById(R.id.imageButton);
        Switch mode = view.findViewById(R.id.switchMode);
        TextView sgWord = view.findViewById(R.id.suggestion);

        // Image Button 비활성화 여부 결정
        do{
            try{
                sleep(1000);
            }catch(Exception e){
                e.printStackTrace();
            }
        }while(!isFin);
        submit.setEnabled(true);

        // suggest 구현
        new Thread() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            public void run() {
                try {
                    sgWord.setText(SuggestWord.suggest(txtPath));
                } catch (Exception e) {
                    e.printStackTrace();
                }
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

        return view;
    }
}