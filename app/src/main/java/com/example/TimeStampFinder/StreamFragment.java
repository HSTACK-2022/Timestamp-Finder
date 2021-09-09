package com.example.TimeStampFinder;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.Math.log10;
import static org.opencv.core.CvType.CV_32F;
import static org.opencv.videoio.Videoio.*;


public class StreamFragment extends Fragment {

    private final String TAG = "STREAM";

    private String fileURI;
    private int fileLength;
    private static int count = 0;
    private String imgName[];
    private Context context;

    private ImageRecyclerAdapter adapter;
    private ProgressBar progress;
    private TextView status;
    private double progValue = 0;

    static {
        System.loadLibrary("opencv_java4");
        System.loadLibrary("native-lib");
    }

    public native int convertNativeLibtoNegative(long addrInput, long addrResult);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Bundle 빼내기 - txtName은 사용하지 않으므로 빼지 않음
        Bundle bundle = getArguments();
        fileURI = bundle.getString("fileURI");
        fileLength = bundle.getInt("fileLength");
        Log.d(TAG, "RESULT frag : " + fileURI +", "+fileLength);

        imgName = new String[fileLength];
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stream, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.image_recyclerView);       // 이미지를 보여주기 위한 recyclerView (단어와 연동 X)
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        progress = view.findViewById(R.id.streamProgressBar);
        status = view.findViewById(R.id.statusText);
        adapter = new ImageRecyclerAdapter();
        recyclerView.setAdapter(adapter);

        status.setText(" 영상에서 장면을 추출하는 중...");

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        context = getActivity().getApplicationContext();

        ExecutorService sceneCut = Executors.newSingleThreadExecutor();
        new SceneCutAsync().executeOnExecutor(sceneCut);
        sceneCut.shutdown();

        Log.d(TAG, "END");
    }

    public class SceneCutAsync extends AsyncTask<Void, Integer, Void> {

        public double getPSNR(Mat I1, Mat I2) {
            Mat s1 = new Mat(I1.rows(), I1.cols(), I1.type());
            Core.absdiff(I1, I2, s1);
            s1.convertTo(s1, CV_32F);
            s1 = s1.mul(s1);
            Scalar s = Core.sumElems(s1);
            double sse = s.val[0] + s.val[1] + s.val[2];

            Log.d(TAG, "see : " + sse);

            if (sse <= 1e-10)
                return 0;
            double mse = sse / (double) (I1.channels() * I1.total());
            double psnr = 10.0 * log10((255 * 255) / mse);
            return psnr;
        }

        public void saveImage(Mat res, int sec) {
            String path = context.getCacheDir() + "/scene" + sec + ".jpg";
            imgName[count++] = path;

            // 이미지의 반전을 막기 위해 자체 반전 코드 추가
            //convertNativeLibtoNegative(res.getNativeObjAddr(),res.getNativeObjAddr());
            Mat out = res.clone();
            Core.not

            Imgcodecs.imwrite(path, res);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            int frameNum = -1;
            double psnrV, CHANGE_DETECT_AUDIO = 10.0;
            VideoCapture cap = new VideoCapture();
            Mat prevFrame = new Mat();
            Mat currFrame = new Mat();
            Mat changeFrame = new Mat();
            Mat result[];

            cap.open(fileURI);
            cap.set(CAP_PROP_FPS, 60);

            while (cap.isOpened()) {
                ++frameNum;
                cap.read(currFrame);

                if (frameNum < 1) {
                    prevFrame = currFrame.clone();
                    changeFrame = currFrame.clone();
                    saveImage(currFrame, 0);
                    continue;
                }

                if (frameNum % 30 == 0) {

                    if (currFrame.rows() == 0 && currFrame.cols() == 0)
                        break;

                    psnrV = getPSNR(prevFrame, currFrame);

                    if (psnrV < CHANGE_DETECT_AUDIO) {
                        changeFrame = currFrame.clone();
                        saveImage(changeFrame, frameNum/30);
                    }

                    prevFrame = currFrame.clone();
                }
                progValue += 100.0/(fileLength*60);
                publishProgress();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... integers){
            progress.setProgress((int)progValue);
        }

        @Override
        protected void onPostExecute(Void v){
            progValue = 100;
            status.setTextColor(Color.GRAY);
            progress.setProgress((int)progValue);
            status.setText(" 장면 추출이 완료되었습니다.");

            // img정렬
            for(int i = 0; i < count; i++){
                // 파일 제목 지정
                String title[] = imgName[i].split("scene|.jpg");
                int secs = Integer.parseInt(title[1]);
                int mins = secs/60;
                secs %= 60;
                Log.d("CHECK", title[1]);

                // 파일 이미지 지정
                File imgFile;
                imgFile = new File(imgName[i]);
                Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                adapter.addItem(new ImageData(bitmap, mins+":"+secs));
            }
            adapter.notifyDataSetChanged();
        }
    }
}