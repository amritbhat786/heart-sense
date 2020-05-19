package polar.com.androidblesdk;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Timer;


import androidx.appcompat.app.AppCompatActivity;



public class vplayerActivity extends Activity {

    File sensorTextFile= null;
    FileInputStream fis= null;
    Timer timer=new Timer();
    String r;
    VideoView videoView;
    int position =-1;

//    File file=new File("/storage/3433-6235/sample/sample.txt");

    public vplayerActivity() throws FileNotFoundException {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vplayer);
        videoView=(VideoView) findViewById(R.id.myPlayer);  //added

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        position=getIntent().getIntExtra("position",-1);
        sensorTextFile = VideoListerActivity.sensorTextFileArrayList.get(position);
        try {
            fis = new FileInputStream(sensorTextFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
//        getSupportActionBar().hide();

        try {
            playerVideo();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }


    public TextView textView;
    class  vpRun implements Runnable{

        public void run() {
            // We first start the label thread and then start the video thread
            new CountDownTimer(30000, 900) {
                // This code gets called once every second for 30 seconds. Total duration should be set based on length of the video.
                public void onTick(long millisUntilFinished) {
                    try {
                        runthread();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }

                public void onFinish() {
                    // Do Nothing
                }
            }.start();

            videoView.start();
        }
    }
    private void playerVideo() throws FileNotFoundException {
        textView=findViewById(R.id.tview);
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);
        videoView.setVideoPath(String.valueOf(VideoListerActivity.videoFileArrayList.get(position)));
        videoView.requestFocus();

        String i="1";

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                // Video code is run in a new thread using a handler
                Handler vh;
                vh = new Handler();
                vpRun vp = new vpRun();
                vh.post(vp);
            }
        });


        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                videoView.setVideoPath(String.valueOf(VideoListerActivity.videoFileArrayList.get(position)));
                // We do nothing here
            }
        });


    }
    public int count=555;

    private final static int DO_UPDATE_TEXT = 0;
    protected void runthread() throws FileNotFoundException {
        //     final File file=new File("/storage/3433-6235/sample/sample.txt");
        //   final FileInputStream fis=new FileInputStream(file);

// Text update is done on UI  thread
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                // ************ Add code to read the values from a file here **************
                int s = 0;
                if (String.valueOf(s) == "13" || String.valueOf(s) == "10")
                {

                }
                else {
                    try {
                        while (fis.read() != -1) {
                            //  int s = 0;
                            try {
                                s = fis.read();
                                System.out.println("Swamy Value is : " + String.valueOf(s));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            textView.setText("Sensor Value:"+s);
                            textView.postInvalidate();

                            break;
                            //    count = count + 100;
                        }
                    } catch (IOException e) {

                    }
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        videoView.stopPlayback();
    }
}