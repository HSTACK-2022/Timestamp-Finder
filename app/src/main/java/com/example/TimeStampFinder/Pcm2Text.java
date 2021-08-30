package com.example.TimeStampFinder;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.google.gson.Gson;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class Pcm2Text {

    private static final String TAG = "PCM2TEXT";

    @RequiresApi(api = Build.VERSION_CODES.O)
    protected String pcm2text(String language, String audioPath, String key) {
        String openApiURL = "http://aiopen.etri.re.kr:8000/WiseASR/Recognition";
        String accessKey = key;
        String languageCode = language;
        String audioFilePath = audioPath;
        String audioContents = null;

        Gson gson = new Gson();

        Map<String, Object> request = new HashMap<>();
        Map<String, String> argument = new HashMap<>();

        try {
            Path path = Paths.get(audioFilePath);
            byte[] audioBytes = Files.readAllBytes(path);
            audioContents = Base64.getEncoder().encodeToString(audioBytes);

            argument.put("language_code", languageCode);
            argument.put("audio", audioContents);

            request.put("access_key", accessKey);
            request.put("argument", argument);

            URL url;
            Integer responseCode;
            String responBody;

            url = new URL(openApiURL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setDoOutput(true);

            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.write(gson.toJson(request).getBytes("UTF-8"));
            wr.flush();
            wr.close();


            responseCode = con.getResponseCode();

            if (responseCode == 200) {
                InputStream is = new BufferedInputStream(con.getInputStream());
                responBody = readStream(is);
                String splits[] = responBody.split("\"");
                Log.d(TAG, "RESULT: "+splits[7]);
                return splits[7];
            } else{
                Log.d(TAG, "ERROR: " + Integer.toString(responseCode));
                return "ERROR: " + Integer.toString(responseCode);
            }
        } catch (Throwable t) {
            Log.d(TAG, "ERROR: " + t.toString());
            return "ERROR: " + t.toString();
        }
    }

    public static String readStream(InputStream in) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader r = new BufferedReader(new InputStreamReader(in),1000);
        for (String line = r.readLine(); line != null; line =r.readLine()){
            sb.append(line);
        }
        in.close();
        return sb.toString();
    }
}

