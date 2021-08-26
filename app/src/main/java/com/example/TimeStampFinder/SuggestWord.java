package com.example.TimeStampFinder;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import com.google.gson.Gson;

public class SuggestWord {

    static public class NameEntity {
        final String text;
        final String type;
        Integer count;
        public NameEntity (String text, String type, Integer count) {
            this.text = text;
            this.type = type;
            this.count = count;
        }
        public String getText(){return text;}
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    static public String suggest(String filePath) throws Exception {
        // 언어 분석 기술 문어/구어 중 한가지만 선택해 사용
        // 언어 분석 기술(문어)
        //String openApiURL = "http://aiopen.etri.re.kr:8000/WiseNLU";
        // 언어 분석 기술(구어)
        String openApiURL = "http://aiopen.etri.re.kr:8000/WiseNLU_spoken";
        String accessKey = "2d40b072-37f1-4317-9899-33e0b3f5fb90";   // 발급받은 API Key
        String analysisCode = "ner";    // 언어 분석 코드
        String text;                    // 분석할 텍스트 데이터
        String res = "";                // 추천 단어를 문자열로 저장
        Gson gson = new Gson();

        //text += Files.readString(Paths.get("test.txt"));
        text = FileWrite.read(filePath);

        Map<String, Object> request = new HashMap<>();
        Map<String, String> argument = new HashMap<>();

        argument.put("analysis_code", analysisCode);
        argument.put("text", text);

        request.put("access_key", accessKey);
        request.put("argument", argument);

        URL url;
        Integer responseCode = null;
        String responBodyJson = null;
        Map<String, Object> responeBody = null;

        try {
            url = new URL(openApiURL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setDoOutput(true);

            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.write(gson.toJson(request).getBytes("UTF-8"));
            wr.flush();
            wr.close();

            responseCode = con.getResponseCode();
            InputStream is = con.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuffer sb = new StringBuffer();

            String inputLine = "";
            while ((inputLine = br.readLine()) != null) {
                sb.append(inputLine);
            }
            responBodyJson = sb.toString();

            // http 요청 오류 시 처리
            if ( responseCode != 200 ) {
                // 오류 내용 출력
                System.out.println("[error] " + responBodyJson);
                return "HTTP ERROR";
            }

            responeBody = gson.fromJson(responBodyJson, Map.class);
            Integer result = ((Double) responeBody.get("result")).intValue();
            Map<String, Object> returnObject;
            List<Map> sentences;

            // 분석 요청 오류 시 처리
            if ( result != 0 ) {
                // 오류 내용 출력
                System.out.println("[error] " + responeBody.get("result"));
                return "RETURN ERROR";
            }

            // 분석 결과 활용
            returnObject = (Map<String, Object>) responeBody.get("return_object");
            sentences = (List<Map>) returnObject.get("sentence");

            Map<String, NameEntity> nameEntitiesMap = new HashMap<String, NameEntity>();
            List<NameEntity> nameEntities = null;

            for( Map<String, Object> sentence : sentences ) {
                // 개체명 분석 결과 수집 및 정렬
                List<Map<String, Object>> nameEntityRecognitionResult = (List<Map<String, Object>>) sentence.get("NE");
                for( Map<String, Object> nameEntityInfo : nameEntityRecognitionResult ) {
                    String name = (String) nameEntityInfo.get("text");
                    NameEntity nameEntity = nameEntitiesMap.get(name);
                    if ( nameEntity == null ) {
                        nameEntity = new NameEntity(name, (String) nameEntityInfo.get("type"), 1);
                        nameEntitiesMap.put(name, nameEntity);
                    } else {
                        nameEntity.count = nameEntity.count + 1;
                    }
                }
            }

            if ( 0 < nameEntitiesMap.size() ) {
                nameEntities = new ArrayList<NameEntity>(nameEntitiesMap.values());
                nameEntities.sort( (nameEntity1, nameEntity2) -> {
                    return nameEntity2.count - nameEntity1.count;
                });
            }

            // 인식된 개채명들 많이 노출된 순으로 출력 ( 최대 4개 )
            System.out.println("");
            nameEntities
                    .stream()
                    .limit(5)
                    .forEach(nameEntity -> {
                        System.out.println("[개체명] " + nameEntity.text + " ("+nameEntity.count+")" );
                    });

            // 각 개체명을 문자열로 저장
            for(int i=0;i<5;i++){
                res += nameEntities.get(i).text;
                res += " ";
            }
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR";
        }
    }
}
