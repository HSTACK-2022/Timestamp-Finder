package com.example.TimeStampFinder;

/*
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.google.gson.Gson;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class VideoCut extends AsyncTask<Object, Object, Object> {

    @Override
    protected Object doInBackground(Object... objects){
        String openApiURL = "http://aiopen.etri.re.kr:8000/VideoParse";
        String openApiURL1 = "http://aiopen.etri.re.kr:8000/VideoParse/status";
        String accessKey = "2d40b072-37f1-4317-9899-33e0b3f5fb90";

        String type = "mp4";  	// 비디오Type
        String file = "video.mp4";  	// 영상 경로
        Gson gson = new Gson();

        Map<String, Object> request = new HashMap<>();
        Map<String, String> argument = new HashMap<>();
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();

        FileBody fileBody = new FileBody(new File(file));

        request.put("access_key", accessKey);
        request.put("argument", argument);

        builder.addPart("uploadfile", fileBody);
        builder.addTextBody("json", gson.toJson(request));

        Integer responseCode = null;
        String responBody = null;


        try {
            CloseableHttpClient http = HttpClients.createDefault();
            HttpPost post = new HttpPost(openApiURL);
            post.setEntity(builder.build());
            CloseableHttpResponse response = http.execute(post);
            StatusLine status;

            try{
                StringBuffer result = new StringBuffer();
                status = response.getStatusLine();
                HttpEntity res = response.getEntity();
                BufferedReader br = new BufferedReader(new InputStreamReader(res.getContent(), Charset.forName("UTF-8")));
                String buffer = null;

                while( (buffer = br.readLine())!=null ){
                    result.append(buffer).append("\r\n");
                }

                responseCode = status.getStatusCode();
                responBody = result.toString();

            }finally{
                response.close();
            }

            System.out.println("[responseCode] " + responseCode);
            System.out.println("[responBody]");
            System.out.println(responBody);

        } catch (MalformedURLException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();

        }
        Log.d("VIDEOCUT", responBody.substring(40, 76));
        return responBody.substring(40,76);
    }
}

 */

