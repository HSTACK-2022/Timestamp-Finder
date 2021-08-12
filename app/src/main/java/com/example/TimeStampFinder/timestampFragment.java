package com.example.TimeStampFinder;

import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.VideoView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.List;

import static android.content.ContentValues.TAG;

public class timestampFragment extends Fragment {

    VideoView tvideoView;
    private RecyclerAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_timestamp,container,false);
        String str;

        //tvideoView = (VideoView)view.findViewById(R.id.videoView);

        Bundle bundle = getArguments();
        str = bundle.getString("uri");

        Log.d(TAG, "RESULT frag:" + str);

        //영상 길이 알아내기
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(str);

        String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        long timeInmillisec = Long.parseLong(time); //예시로 7531 이면
        long duration = timeInmillisec / 1000; // 7.531 초
        long hours = duration / 3600;
        //long hours = TimeUnit.MILLISECONDS.toHours(timeInmillisec); 위랑 동일. TimeUnit 함수 쓴 것 뿐
        long minutes = (duration - hours * 3600) / 60; // 1분에 60000 msec임
        long seconds = duration - (hours * 3600 + minutes * 60);
        System.out.println("TIME length"+ time +"duration : "+ duration +" hours: " + hours + ", minutes: " + minutes + ", seconds: " + seconds);



        RecyclerView recyclerView = view.findViewById(R.id.recyclerView); //얘는 view대신 getView를 써야한다 이유는 위에서 return 했기 때문
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        adapter = new RecyclerAdapter();
        recyclerView.setAdapter(adapter);


        List<String> listTitle = Arrays.asList("출력 단어");
        List<String> listContent = Arrays.asList(
                "출력된 단어가 있는 문장을 보여줍니다."
        );
        List<Integer> listResId = Arrays.asList(
                R.drawable.other
        );
        for (int i = 0; i < listTitle.size(); i++) {
            // 각 List의 값들을 data 객체에 set 해줍니다.
            Data data = new Data();
            data.setTitle(listTitle.get(i));
            data.setContent(listContent.get(i));
            data.setResId(listResId.get(i));

            // 각 값이 들어간 data를 adapter에 추가합니다.
            adapter.addItem(data);
        }

        // adapter의 값이 변경되었다는 것을 알려줍니다.
        adapter.notifyDataSetChanged();

// 이거 컨버트에 옮겨서 findViewId 이용해보기
//        버튼 누르면 해당 시간으로 가는 함수
//        Button tbutton = (Button) view.findViewById(R.id.timebtn);
//        tbutton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//               // videoView = v.findViewById(R.id.videoView);
//                tvideoView.seekTo((int) timeInmillisec/2);
//                tvideoView.start();
//                System.out.println("hours: " + hours + ", minutes: " + minutes + ", seconds: " + seconds);
////                System.out.format("%02d:%02d:%02d", hours, minutes, seconds);
//            }
//        });

        //리사이클러 부분 https://dev-imaec.tistory.com/27

        // Inflate the layout for this fragment
        return view;
    }

}