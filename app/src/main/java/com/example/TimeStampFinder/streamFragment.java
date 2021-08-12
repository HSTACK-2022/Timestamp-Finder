package com.example.TimeStampFinder;

import android.os.Bundle;
import android.widget.VideoView;

import androidx.fragment.app.Fragment;


public class streamFragment extends Fragment {

    VideoView tvideoView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.fragment_stream, container, false);
//
//        String str;
//
//        tvideoView = (VideoView)view.findViewById(R.id.videoView);
//
//        Bundle bundle = getArguments();
//        str = bundle.getString("uri");
//
//        Log.d(TAG, "RESULT frag:" + str);
//
//        //영상 길이 알아내기
//        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
//        retriever.setDataSource(str);
//
//        String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
//        long timeInmillisec = Long.parseLong(time); //예시로 7531 이면
//        long duration = timeInmillisec / 1000; // 7.531 초
//        long hours = duration / 3600;
//        //long hours = TimeUnit.MILLISECONDS.toHours(timeInmillisec); 위랑 동일. TimeUnit 함수 쓴 것 뿐
//        long minutes = (duration - hours * 3600) / 60; // 1분에 60000 msec임
//        long seconds = duration - (hours * 3600 + minutes * 60);
//        System.out.println("TIME length"+ time +"duration : "+ duration +" hours: " + hours + ", minutes: " + minutes + ", seconds: " + seconds);
//
//
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
//
////        Intent intent = new Intent(streamFragment.class , convertActivity.class);
////        startActivityForResult(intent, 1000);
//        // Inflate the layout for this fragment
//        return view;
//    }


}