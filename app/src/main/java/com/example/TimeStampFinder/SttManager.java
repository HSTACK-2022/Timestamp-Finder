package com.example.TimeStampFinder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.Thread.sleep;

/*
public class SttManager extends AsyncTask<Object, Integer, Boolean> {

    private static String TAG = "SttManager";

    private int size = 5;   // 최대로 만들 수 있는 스레드의 수
    private String keys[] = {"2d40b072-37f1-4317-9899-33e0b3f5fb90",};

    @SuppressLint("WrongThread")
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected Boolean doInBackground(Object... objects) {
        
        // 들어온 총 파일의 수를 5개로 분할하고, 각각 5개의 AsyncTask로 전달
        int i;
        int asyncNum = (int)objects[0]/size;        // 1개의 스레드에 보낼 최대 파일의 수
        FileWrite fw = (FileWrite)objects[1];       // 파일 작성에 필요한 FileWriter
        String txtPath = (String)objects[2];       // 모든 파일을 취합해 만들 최종 텍스트 파일의 경로
        Context context = (Context)objects[3];

        String tempPath[] = new String[size];

        ExecutorService threadPool = Executors.newFixedThreadPool(size);
        SttAsynctask sat[] = new SttAsynctask[size];

        for(i=0;i<size;i++){

            int startIndex = i*asyncNum;
            int endIndex;

            // 오디오 시작, 종료 index 지정
            if(i==size-1)   endIndex = (int)objects[0]-1;
            else            endIndex = startIndex+asyncNum-1;

            FileWrite temp = new FileWrite("temp"+i+".txt", context);       // 각 스레드별 임시 파일 생성
            tempPath[i] = temp.create();

            new SttAsynctask().executeOnExecutor(threadPool, startIndex, endIndex, temp, tempPath[i], keys[1]);
        }

        // check Fin
        while(i<size){
            if(sat[i].getStatus()==Status.FINISHED)
                i++;
            try{sleep(1000);}
            catch(Exception e){e.printStackTrace();}
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
        // doInBackground 에서 받아온 total 값 사용 장소
        Log.d(TAG, b.toString());
    }


    // API로 직접적으로 보내는 스레드
    public class SttAsynctask extends AsyncTask<Object, Integer, Boolean>{
        private int size = 5;   // 저쪽에서 보내는 최대 파일 개수
        private int start;       // 시작 파일 번호
        private int end;         // 끝 파일 번호
        private FileWrite fw;    // 임시 저장을 위한 파일
        private String filePath;    // 파일 경로
        private String key;         // API Access Key

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        protected Boolean doInBackground(Object[] objects) {
            int start = (int)objects[0];
            int end = (int)objects[0];
            FileWrite fw = (FileWrite)objects[2];
            String filePath = (String)objects[3];
            String key = (String)objects[4];

            for(int i=start; i<=end; i++){
                String result = new Pcm2Text().pcm2text("korean", "/storage/emulated/0/Music/"+(i+1)+"get.wav", key);
                fw.write(i+"", filePath);       // index 기록
                fw.write(result, filePath);
            }

            return true;
        }
    }
}



        // 잘린 wav 파일이 모두 들어왔다고 가정하고 스레드로 넘기기
        int size = 12;      // 파일의 개수를 임의로 지정하자.
        int maxFile = 5;    // 하나의 스레드에 보낼 최대 파일의 수
        int num = (size/maxFile)+1;     // 만들어질 총 스레드의 수
        isFin = new boolean[num+1];     // 해당 스레드의 종료 여부를 나타낼 변수
        String tmpPath[] = new String[num+1];   // 임시 텍스트 파일을 저장할 변수

        SttThread st[] = new SttThread[num+1];

        new Thread(){
            @Override
            public void run(){
                for(int i=1;i<=num;i++){
                    isFin[i] = false;

                    // 새로운 FileWriter 생성
                    FileWrite fw = new FileWrite("temp"+i+".txt", getApplicationContext());
                    tmpPath[i] = fw.create();
                    // 새로운 스레드 생성 : 5개의 오디오파일을 기록하는 스레드
                    if(i==num)      st[i] = new SttThread((i-1)*maxFile+1, size, fw, tmpPath[i], key[i%3]);
                    else            st[i] = new SttThread((i-1)*maxFile+1, i*maxFile, fw, tmpPath[i], key[i%3]);

                    Log.d("THREADCHECK : MADE", String.valueOf(i));
                }

                new Thread(){
                    @Override
                    public void run(){
                        Log.d("THREADCHECK : START", "1");
                        st[1].run();
                    }
                }.start();

                new Thread(){
                    @Override
                    public void run(){
                        Log.d("THREADCHECK : START", "2");
                        st[2].run();
                    }
                }.start();

                new Thread(){
                    @Override
                    public void run(){
                        Log.d("THREADCHECK : START", "3");
                        st[3].run();
                    }
                }.start();

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

 */
