package com.example.TimeStampFinder;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.util.Log;
import android.widget.ProgressBar;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.Math.log10;
import static org.opencv.core.CvType.CV_32F;
import static org.opencv.videoio.Videoio.*;


public class StreamFragment extends Fragment {

    private final String TAG = "STREAM";

    private String fileURI;
    private static int count = -1;
    private Context context;

    private ProgressBar progress;
    private int progValue = 0;

    static {
        System.loadLibrary("opencv_java4");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Bundle 빼내기 - txtName은 사용하지 않으므로 빼지 않음
        Bundle bundle = getArguments();
        fileURI = bundle.getString("fileURI");
        Log.d(TAG, "RESULT frag : " + fileURI);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stream, container, false);

        progress = view.findViewById(R.id.progressBar);
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

        public void saveImage(Mat res, double nowMsec) {
            count++;
            String path = context.getCacheDir() + "/scene" + nowMsec + ".jpg";
            Imgcodecs.imwrite(path, res);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            int frameNum = -1;
            double totalFrames, nowMsec;
            double psnrV, CHANGE_DETECT_AUDIO = 10.0;
            VideoCapture cap = new VideoCapture();
            Mat prevFrame = new Mat();
            Mat currFrame = new Mat();
            Mat changeFrame = new Mat();
            Mat result[];

            cap.open(fileURI);
            Log.d(TAG, fileURI);

            totalFrames = cap.get(CAP_PROP_FRAME_COUNT);
            Log.d(TAG, "TFS : " + totalFrames);

            while (cap.isOpened()) {
                ++frameNum;
                cap.read(currFrame);
                //progValue = (int)(cap.get(CAP_PROP_POS_FRAMES)*100)/totalFrames;
                //Log.d(TAG, progValue+"");

                if (frameNum < 1) {
                    prevFrame = currFrame.clone();
                    changeFrame = currFrame.clone();
                    saveImage(currFrame, 0.0);
                    continue;
                }

                if (frameNum % 30 == 0) {
                    nowMsec = cap.get(CAP_PROP_POS_MSEC);

                    if (currFrame.rows() == 0 && currFrame.cols() == 0)
                        break;

                    psnrV = getPSNR(prevFrame, currFrame);

                    if (psnrV < CHANGE_DETECT_AUDIO) {
                        changeFrame = currFrame.clone();
                        saveImage(changeFrame, nowMsec);
                    }

                    prevFrame = currFrame.clone();
                }
                publishProgress();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... integers){
            progress.setProgress(progValue);
        }

        @Override
        protected void onPostExecute(Void v){
            Log.d(TAG, "Async Finished.");
        }
    }
}