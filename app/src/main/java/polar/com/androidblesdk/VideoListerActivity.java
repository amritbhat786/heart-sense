package polar.com.androidblesdk;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import polar.com.androidblesdk.MyAdapter;
import polar.com.androidblesdk.R;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

public class VideoListerActivity extends Activity {

    RecyclerView myRecyclerView;
    MyAdapter obj_adapter;
    public static int REQUEST_PERMISSION = 1;
    File directory;
    boolean boolean_perm;
    public static ArrayList<File> videoFileArrayList = new ArrayList<File>();
    public static ArrayList<File> sensorTextFileArrayList = new ArrayList<File>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_record);

        myRecyclerView = (RecyclerView) findViewById(R.id.listVideoRecycler);
        directory = MainActivity.appDirPath;

        GridLayoutManager manager= new GridLayoutManager(VideoListerActivity.this,2);
        myRecyclerView.setLayoutManager(manager);

        permissionForVideo();

    }

    private void permissionForVideo() {
        if((ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED)) {
            if(ActivityCompat.shouldShowRequestPermissionRationale(VideoListerActivity.this,Manifest.permission.READ_EXTERNAL_STORAGE)){

            }
            else{
                ActivityCompat.requestPermissions(VideoListerActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},REQUEST_PERMISSION);
            }
        }
        else {
            boolean_perm=true;
            videoFileArrayList = new ArrayList<>();
            sensorTextFileArrayList = new ArrayList<>();
            getFile(directory);

            obj_adapter=new MyAdapter(getApplicationContext(),videoFileArrayList,sensorTextFileArrayList);

            myRecyclerView.setAdapter(obj_adapter);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == REQUEST_PERMISSION){
            if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                boolean_perm=true;
                videoFileArrayList = new ArrayList<>();
                sensorTextFileArrayList = new ArrayList<>();
                getFile(directory);
                obj_adapter=new MyAdapter(getApplicationContext(),videoFileArrayList,sensorTextFileArrayList);
                myRecyclerView.setAdapter(obj_adapter);

            }
            else{
                Toast.makeText(this,"Allow permissions",Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void getFile(File directory){
        File listFile[] = directory.listFiles();

        if(listFile!=null&&listFile.length>0){

            for(int i=0;i<listFile.length;i++){
                System.out.println("List file");
                System.out.println(listFile[i]);
                if(listFile[i].isDirectory()){

                    //   System.out.println("THE blood UCK I AM HERE");
                    getFile(listFile[i]);

                }

                else {

                    boolean_perm=false;
                    if(listFile[i].getName().endsWith(".mp4")){
                        System.out.println("Video file found");
                        System.out.println(listFile[i].getName());
                        videoFileArrayList.add(listFile[i]);
                    }
                    if(listFile[i].getName().endsWith(".txt")){
                        System.out.println("Text file found");
                        System.out.println(listFile[i].getName());
                        sensorTextFileArrayList.add(listFile[i]);
                    }
                }
            }

        }
    }
}
