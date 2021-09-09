package com.example.TimeStampFinder;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Iterator;

public class SearchWord {

    private static String word;			// 찾고자 하는 단어(,로 구분)
    private static String path;         // 파일을 저장한 경로
    private boolean strongMode;	        // 강한 검색 여부
    private final String TAG = "SEARCH";

    // 생성자
    public SearchWord() {
        this.word = null;
        this.strongMode = false;
    }
    public SearchWord(String word, String path, boolean strongMode) {
        this.word = word;
        this.path = path;
        this.strongMode = strongMode;
    }

    // 모드 설정하기
    public void setMode(boolean strongMode) {
        this.strongMode = strongMode;
    }

    // 단어를 찾아 HashMap에 저장 후 리턴
    public HashMap findWord() throws Exception {
        String txtIndex = "";
        String txtStr = "";
        String words[] = word.split(",");
        BufferedReader br = new BufferedReader(new FileReader(new File(path)));
        HashMap<String, String> result  = new HashMap<String, String>();

        // 파일의 모든 줄을 돌며 단어 검색
        while((txtIndex = br.readLine())!=null){

            int i;
            txtStr = br.readLine();			// 각 줄의 내용 저장
            Log.d(TAG, "txtStr : "+txtStr);

            // 강한 검색
            if(strongMode) {
                for(i=0;i<words.length;i++) {
                    if(txtStr.indexOf(words[i])==-1)
                        break;
                }
                if(i==words.length) {	// 여러 단어가 다 있어야 저장
                    result.put(txtIndex, txtStr);
                }
            }

            // 약한 검색
            else {
                for(i=0;i<words.length;i++) {
                    if(txtStr.indexOf(words[i])!=-1) {
                        result.put(txtIndex, txtStr);
                    }
                }
            }
        }

        //buffer close
        br.close();

        return result;
    }
}
