package polar.com.androidblesdk;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Timer;


import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;



public class vplayerActivity extends Activity {

    File sensorTextFile= null;
    BufferedReader fis= null;
    FileReader fr = null;
    BufferedReader nol= null; //for calculating number of lines
    Timer timer=new Timer();
    String r;
    VideoView videoView;
    int position =-1;
    public String strdisp;
//    File file=new File("/storage/3433-6235/sample/sample.txt");

    public vplayerActivity() throws FileNotFoundException {
    }

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vplayer);
        videoView=(VideoView) findViewById(R.id.myPlayer);  //added

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        position=getIntent().getIntExtra("position",-1);
        sensorTextFile = VideoListerActivity.sensorTextFileArrayList.get(position);
        try {
            fr=new FileReader(sensorTextFile);
            fis = new BufferedReader(fr);

           strdisp=fis.readLine();
            System.out.println("FIS SET");
        } catch (IOException e) {
            System.out.println("NOT SET");
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

        @RequiresApi(api = Build.VERSION_CODES.O)
        public void run() {
            //To find duration of video
            MediaMetadataRetriever retriever=new MediaMetadataRetriever();
            retriever.setDataSource(String.valueOf(VideoListerActivity.videoFileArrayList.get(position)));
            String time=retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            long timeInmillis=Long.parseLong(time);
            System.out.println("TIME IN MILLI");
            System.out.println(timeInmillis);
            //To find number of lines
            List<String> lines = null;
            try {
                lines = Files.readAllLines(Paths.get(String.valueOf(VideoListerActivity.sensorTextFileArrayList.get(position))), Charset.defaultCharset());
            } catch (IOException e) {
                e.printStackTrace();
            }
            int nooflines=lines.size();
            System.out.println("Number of lines");
            System.out.println(nooflines);
            long target;
            target=timeInmillis/nooflines;
            System.out.println("Target");
            System.out.println(target);
            // We first start the label thread and then start the video thread
            new CountDownTimer(100000, target) {
                // This code gets called once every second for 30 seconds. Total duration should be set based on length of the video.
                public void onTick(long millisUntilFinished) {
                    try {
                        runthread();
                        System.out.println("Run thread success");
                    } catch (FileNotFoundException e) {
                        System.out.println("Run thread not success");
                        e.printStackTrace();
                    }
                }

                public void onFinish() {
                    Intent intent = new Intent(String.valueOf(MainActivity.class));
                    startActivity(intent);
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
     /*   videoView.setPlayPauseListener(new StateBroadcastingVideoView.PlayPauseListener() {
            @Override
            public void onPlay() {

            }

            @Override
            public void onPause() {
                Toast.makeText(vplayerActivity.this,"Video pause",Toast.LENGTH_SHORT).show();
            }
        });
       */
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
                try {
                    fr=new FileReader(sensorTextFile);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                fis = new BufferedReader(fr);
//                Intent intent = new Intent(String.valueOf(vplayerActivity.class));
//                startActivity(intent);
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
                char c = 0;
                System.out.println("HERE!!");
                if (String.valueOf(s) == "13" || String.valueOf(s) == "10")
                {

                }
                else {
                    try {
                        System.out.println("Before try");

                        while (strdisp!="\0"&&videoView.isPlaying()) {
                            //  int s = 0;

                            System.out.println("Inside while");
                            // c = fis.readLine();
                            // c= (char)s;
                            System.out.println("Value is : " +strdisp);

                            textView.setText("Sensor Value:"+strdisp);
                            Thread.sleep(1000);
                            textView.postInvalidate();
                             strdisp=fis.readLine();
                            break;
                            //    count = count + 100;
                        }
                    } catch (IOException | InterruptedException e) {

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