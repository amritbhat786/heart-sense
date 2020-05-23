package polar.com.androidblesdk;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.reactivestreams.Publisher;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Function;
import polar.com.sdk.api.PolarBleApi;
import polar.com.sdk.api.PolarBleApiCallback;
import polar.com.sdk.api.PolarBleApiDefaultImpl;
import polar.com.sdk.api.errors.PolarInvalidArgument;
import polar.com.sdk.api.model.PolarAccelerometerData;
import polar.com.sdk.api.model.PolarDeviceInfo;
import polar.com.sdk.api.model.PolarEcgData;
import polar.com.sdk.api.model.PolarExerciseEntry;
import polar.com.sdk.api.model.PolarHrData;
import polar.com.sdk.api.model.PolarOhrPPGData;
import polar.com.sdk.api.model.PolarOhrPPIData;
import polar.com.sdk.api.model.PolarSensorSetting;

public class MainActivity extends Activity implements SensorEventListener {
    private final static String TAG = MainActivity.class.getSimpleName();

    // Light sensor meta
    private SensorManager mSensorManager;
    //private Sensor mSensorProximity;
    private Sensor mSensorLight;

    // Polar sensor meta
    PolarBleApi api;
    Disposable broadcastDisposable;
    Disposable ecgDisposable;
    Disposable accDisposable;
    Disposable ppgDisposable;
    Disposable ppiDisposable;
    Disposable scanDisposable;
    String DEVICE_ID = "5529C22F"; // or bt address like F5:A7:B8:EF:7A:D1 // TODO replace with your device id
    Disposable autoConnectDisposable;
    PolarExerciseEntry exerciseEntry;
    public static File appDirPath;
    static final int REQUEST_VIDEO_CAPTURE = 1;
    public File file;
    public FileOutputStream fstream;
    public OutputStreamWriter myOutWriter = null;
    public Boolean sensorConnected=Boolean.TRUE;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        mSensorLight=mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        appDirPath = getApplicationContext().getFilesDir();
        appDirPath = new File((appDirPath == null ? "" : (appDirPath.getAbsolutePath() + "/")));
        // Notice PolarBleApi.ALL_FEATURES are enabled
        api = PolarBleApiDefaultImpl.defaultImplementation(this, PolarBleApi.ALL_FEATURES);
        api.setPolarFilter(false);

        final Button connect = this.findViewById(R.id.connect_button);
        final Button disconnect = this.findViewById(R.id.disconnect_button);
        final Button recordVideo = this.findViewById(R.id.record_video);
//        final Button autoConnect = this.findViewById(R.id.auto_connect_button);
//        final Button ecg = this.findViewById(R.id.ecg_button);
//        final Button acc = this.findViewById(R.id.acc_button);
//        final Button ppg = this.findViewById(R.id.ohr_ppg_button);
//        final Button ppi = this.findViewById(R.id.ohr_ppi_button);
        final Button scan = this.findViewById(R.id.scan_button);
        final Button listVideos = this.findViewById(R.id.listVideos);


        api.setApiLogger(s -> Log.d(TAG,s));

        Log.d(TAG,"version: " + PolarBleApiDefaultImpl.versionInfo());

        api.setApiCallback(new PolarBleApiCallback() {
            @Override
            public void blePowerStateChanged(boolean powered) {
                Log.d(TAG,"BLE power: " + powered);
            }

            @Override
            public void deviceConnected(@NonNull PolarDeviceInfo polarDeviceInfo) {
                Log.d(TAG,"CONNECTED: " + polarDeviceInfo.deviceId);
                DEVICE_ID = polarDeviceInfo.deviceId;
                sensorConnected = Boolean.TRUE;
            }

            @Override
            public void deviceConnecting(@NonNull PolarDeviceInfo polarDeviceInfo) {
                Log.d(TAG,"CONNECTING: " + polarDeviceInfo.deviceId);
                DEVICE_ID = polarDeviceInfo.deviceId;
            }

            @Override
            public void deviceDisconnected(@NonNull PolarDeviceInfo polarDeviceInfo) {
                Log.d(TAG,"DISCONNECTED: " + polarDeviceInfo.deviceId);
                sensorConnected = Boolean.FALSE;
                ecgDisposable = null;
                accDisposable = null;
                ppgDisposable = null;
                ppiDisposable = null;
                MainActivity.super.onStop();
            }

            @Override
            public void ecgFeatureReady(@NonNull String identifier) {
                Log.d(TAG,"ECG READY: " + identifier);
                // ecg streaming can be started now if needed
            }

            @Override
            public void accelerometerFeatureReady(@NonNull String identifier) {
                Log.d(TAG,"ACC READY: " + identifier);
                // acc streaming can be started now if needed
            }

            @Override
            public void ppgFeatureReady(@NonNull String identifier) {
                Log.d(TAG,"PPG READY: " + identifier);
                // ohr ppg can be started
            }

            @Override
            public void ppiFeatureReady(@NonNull String identifier) {
                Log.d(TAG,"PPI READY: " + identifier);
                // ohr ppi can be started
            }

            @Override
            public void biozFeatureReady(@NonNull String identifier) {
                Log.d(TAG,"BIOZ READY: " + identifier);
                // ohr ppi can be started
            }

            @Override
            public void hrFeatureReady(@NonNull String identifier) {
                Log.d(TAG,"HR READY: " + identifier);
                // hr notifications are about to start
            }

            @Override
            public void disInformationReceived(@NonNull String identifier,@NonNull UUID uuid,@NonNull String value) {
                Log.d(TAG,"uuid: " + uuid + " value: " + value);

            }

            @Override
            public void batteryLevelReceived(@NonNull String identifier, int level) {
                Log.d(TAG,"BATTERY LEVEL: " + level);

            }

            @Override
            public void hrNotificationReceived(@NonNull String identifier,@NonNull PolarHrData data) {
                Log.d(TAG,"HR value: " + data.hr + " rrsMs: " + data.rrsMs + " rr: " + data.rrs + " contact: " + data.contactStatus + "," + data.contactStatusSupported);
                try {
                    Log.d(TAG, "Sensor connected Flag: "+sensorConnected.toString());
                    Log.d(TAG,"Output writer object: "+myOutWriter);
                    if(sensorConnected == Boolean.TRUE && myOutWriter != null) {
                        long currentTime = System.currentTimeMillis();
                        Timestamp ts = new Timestamp(currentTime);
                        Log.d(TAG,"Trying writing sensor data to file...");
                        Log.d(String.valueOf(ts), "onSensorChanged: "+String.valueOf(data.hr));
                        myOutWriter.append(String.valueOf(ts));
                        myOutWriter.append(String.valueOf(data.hr));
                        myOutWriter.append("\n");
                        myOutWriter.flush();
                        //  dynamview=(TextView)findViewById(R.id.ttview);
                        // dynamview.setText(val);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d("exception", "onSensorChanged: ");
                }
            }

            @Override
            public void polarFtpFeatureReady(@NonNull String s) {
                Log.d(TAG,"FTP ready");
            }
        });

        recordVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (null == savedInstanceState && sensorConnected == Boolean.TRUE) {
                    File absoluteAppDirPath = new File((appDirPath.getAbsolutePath()+"/")
                            +System.currentTimeMillis());
                    Boolean mkNewDirFlag = absoluteAppDirPath.mkdir();
                    Log.d(TAG,"Make new Dir Status: "+mkNewDirFlag);
                        try {
                            String fileName ="SensorTimeSeries.txt";
                            file = new File(absoluteAppDirPath,fileName);
                            file.createNewFile();
                            Log.d(TAG,"New text file created, path: "+file);
                            fstream=new FileOutputStream(file);
                            myOutWriter=new OutputStreamWriter(fstream);

                            // Instantiate Video record session
                            getFragmentManager().beginTransaction()
                                    .replace(R.id.container, Camera2VideoFragment.newInstance(absoluteAppDirPath.toString()))
                                    .commit();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                else{
                    Toast.makeText(getApplicationContext(), "Cannot start video recording as " +
                            "sensor not connected! Connect sensor and try again.", Toast.LENGTH_SHORT).show();
                    }
                }
        });

        listVideos.setOnClickListener( new View.OnClickListener(){
           @Override
           public void onClick(View view) {
               openVideoListerActivity();
           }
        });

        connect.setOnClickListener(v -> {
            try {
                api.connectToDevice(DEVICE_ID);
            } catch (PolarInvalidArgument polarInvalidArgument) {
                polarInvalidArgument.printStackTrace();
            }
        });

        disconnect.setOnClickListener(view -> {
            try {
                api.disconnectFromDevice(DEVICE_ID);
                sensorConnected = Boolean.FALSE;
                this.onStop();
            } catch (PolarInvalidArgument polarInvalidArgument) {
                polarInvalidArgument.printStackTrace();
            }
        });


        scan.setOnClickListener(view -> {
            if(scanDisposable == null) {
                scanDisposable = api.searchForDevice().observeOn(AndroidSchedulers.mainThread()).subscribe(
                        polarDeviceInfo -> Log.d(TAG, "polar device found id: " + polarDeviceInfo.deviceId + " address: " + polarDeviceInfo.address + " rssi: " + polarDeviceInfo.rssi + " name: " + polarDeviceInfo.name + " isConnectable: " + polarDeviceInfo.isConnectable),
                        throwable -> Log.d(TAG, "" + throwable.getLocalizedMessage()),
                        () -> Log.d(TAG, "complete")
                );
            }else{
                scanDisposable.dispose();
                scanDisposable = null;
            }
        });
//
//        startH10Recording.setOnClickListener(view -> api.startRecording(DEVICE_ID,"TEST_APP_ID", PolarBleApi.RecordingInterval.INTERVAL_1S, PolarBleApi.SampleType.HR).subscribe(
//                () -> Log.d(TAG,"recording started"),
//                throwable -> Log.e(TAG,"recording start failed: " + throwable.getLocalizedMessage())
//        ));
//
//        stopH10Recording.setOnClickListener(view -> api.stopRecording(DEVICE_ID).subscribe(
//                () -> Log.d(TAG,"recording stopped"),
//                throwable -> Log.e(TAG,"recording stop failed: " + throwable.getLocalizedMessage())
//        ));
//
//        H10RecordingStatus.setOnClickListener(view -> api.requestRecordingStatus(DEVICE_ID).subscribe(
//                pair -> Log.d(TAG,"recording on: " + pair.first + " ID: " + pair.second),
//                throwable -> Log.e(TAG, "recording status failed: " + throwable.getLocalizedMessage())
//        ));



        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && savedInstanceState == null) {
            this.requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if(requestCode == 1) {
            Log.d(TAG,"bt ready");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        api.backgroundEntered();
    }

    @Override
    public void onResume() {
        super.onResume();
        api.foregroundEntered();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        api.shutDown();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    public void openVideoListerActivity(){
        Intent intent = new Intent(this,VideoListerActivity.class);
        startActivity(intent);
    }

    public void connectToDevice(String device_ID){
        try {
            api.connectToDevice(device_ID);
        } catch (PolarInvalidArgument polarInvalidArgument) {
            polarInvalidArgument.printStackTrace();
        }
    }

//    public void writeSensorValToFile(){
//
//        Log.d(val, "onSensorChanged: ");
//        if(myOutWriter!=null) {
//            try {
//                val1 = System.currentTimeMillis();
//                Timestamp ts = new Timestamp(val1);
//                Log.d(String.valueOf(ts), "onSensorChanged: ");
//                myOutWriter.append(String.valueOf(ts));
//                myOutWriter.append(val);
//                myOutWriter.append("\n");
//                //myOutWriter.close();
//                myOutWriter.flush();
//                Log.d(val, "on ");
//                //  dynamview=(TextView)findViewById(R.id.ttview);
//                // dynamview.setText(val);
//
//            } catch (IOException e) {
//                e.printStackTrace();
//                Log.d("exception", "onSensorChanged: ");
//            }
//        }
//    }

    String val;
    long val1;
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        int sensorType = sensorEvent.sensor.getType();



        float currentValue=sensorEvent.values[0];
        switch(sensorType){
            case Sensor.TYPE_LIGHT:
            {

                //  mTextSensorLight=(TextView)findViewById(R.id.label_light);
//                mTextSensorLight.setText(getResources().getString(R.string.label_light,currentValue));
                //mTextSensorLight.setText(getResources().getString(R.string.label_light,currentValue));
                //mTextSensorLight.setText("Trial");
                //  OutputStreamWriter myOutWriter = new OutputStreamWriter(fstream);

                TextView dynamview;
                val=getResources().getString(R.string.label_light,currentValue);
                Log.d(val, "onSensorChanged: ");
                if(myOutWriter!=null) {
                    try {
                        val1 = System.currentTimeMillis();
                        Timestamp ts = new Timestamp(val1);
                        Log.d(String.valueOf(ts), "onSensorChanged: ");
                        myOutWriter.append(String.valueOf(ts));
                        myOutWriter.append(val);
                        myOutWriter.append("\n");
                        //myOutWriter.close();
                        myOutWriter.flush();
                        Log.d(val, "on ");
                        //  dynamview=(TextView)findViewById(R.id.ttview);
                        // dynamview.setText(val);

                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.d("exception", "onSensorChanged: ");
                    }
                }

                //       TextView dynamview;
                //     dynamview=(TextView)findViewById(R.id.ttview);

                //   dynamview.setText(val);


            }
            break;
            default:
        }
    }
    @Override
    protected void onStop(){
        Log.d(TAG,"CLOSING File output handler!!!!!!!");
        myOutWriter = null;
        super.onStop();
//        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
