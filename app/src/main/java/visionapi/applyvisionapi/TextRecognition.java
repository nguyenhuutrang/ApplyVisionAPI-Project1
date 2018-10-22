package visionapi.applyvisionapi;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.IOException;

public class TextRecognition extends AppCompatActivity {

    SurfaceView cameraView;
    TextView textView;
    CameraSource cameraSource;
    final int requestCameraPermistionID = 1001;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case requestCameraPermistionID:{
                if(grantResults[0] ==PackageManager.PERMISSION_GRANTED){
                    if(ActivityCompat.checkSelfPermission( this,Manifest.permission.CAMERA ) != PackageManager.PERMISSION_GRANTED)
                        return;
                }
                try{
                    cameraSource.start(cameraView.getHolder());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_text_recognition );

        cameraView = (SurfaceView)findViewById( R.id.sfv_text );
        textView = (TextView)findViewById( R.id.txtv_text );

        TextRecognizer textRecognizer = new TextRecognizer.Builder( getApplicationContext() ).build();
        if(!textRecognizer.isOperational()){
            Log.w( "TextRecognition", "Detector Dependencies chưa có sẵn");
        }else{
            cameraSource = new CameraSource.Builder( getApplicationContext(), textRecognizer)
                    .setFacing( CameraSource.CAMERA_FACING_BACK )
                    .setRequestedPreviewSize( 1280,1024 )
                    .setRequestedFps( 15.0f )
                    .setAutoFocusEnabled( true )
                    .build();

            cameraView.getHolder().addCallback( new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    try{
                        if(ActivityCompat.checkSelfPermission( getApplicationContext(),Manifest.permission.CAMERA ) != PackageManager.PERMISSION_GRANTED){
                            ActivityCompat.requestPermissions( TextRecognition.this,
                                    new String[]{Manifest.permission.CAMERA},
                                    requestCameraPermistionID);
                            return;
                        }
                        cameraSource.start(cameraView.getHolder());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                    cameraSource.stop();
                }
            } );

            textRecognizer.setProcessor( new Detector.Processor<TextBlock>() {
                @Override
                public void release() {

                }

                @Override
                public void receiveDetections(Detector.Detections<TextBlock> detections) {
                    final SparseArray<TextBlock> items = detections.getDetectedItems();
                    if(items.size() != 0){
                        textView.post( new Runnable() {
                            @Override
                            public void run() {
                                StringBuilder stringBuilder = new StringBuilder(  );
                                for(int i = 0; i < items.size(); ++i){
                                    TextBlock item = items.valueAt( i );
                                    stringBuilder.append( item.getValue() );
                                    stringBuilder.append( "\n" );
                                }
                                textView.setText(stringBuilder.toString());
                            }
                        } );
                    }
                }
            } );
        }
    }
}
