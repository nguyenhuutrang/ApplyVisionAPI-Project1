package visionapi.applyvisionapi;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;

public class QRCode extends AppCompatActivity {

    SurfaceView cameraPreview;
    TextView txtResult;
    BarcodeDetector barcodeDetector;
    CameraSource cameraSource;
    final int requestCameraPermissionID = 1001;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case requestCameraPermissionID: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission( this, Manifest.permission.CAMERA ) != PackageManager.PERMISSION_GRANTED) {

                        return;
                    }
                    try {
                        cameraSource.start( cameraPreview.getHolder() );
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_qrcode );

        cameraPreview = findViewById( R.id.sfv_qr_code );
        txtResult = findViewById( R.id.txtv_result );

        barcodeDetector = new BarcodeDetector.Builder( this )
                .setBarcodeFormats( Barcode.QR_CODE )
                .build();
        cameraSource = new CameraSource.Builder( this, barcodeDetector )
                .setFacing( CameraSource.CAMERA_FACING_BACK )
                .setAutoFocusEnabled( true )
                .setRequestedFps( 2.0f )
                .setRequestedPreviewSize( 1280, 1024)
                .build();

        // them cac su kien xu ly
        cameraPreview.getHolder().addCallback( new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (ActivityCompat.checkSelfPermission( getApplicationContext(), Manifest.permission.CAMERA ) != PackageManager.PERMISSION_GRANTED) {
                    // Request permission
                    ActivityCompat.requestPermissions( QRCode.this,
                            new String[]{Manifest.permission.CAMERA} ,
                            requestCameraPermissionID);
                    return;
                }
                try {
                    cameraSource.start( cameraPreview.getHolder() );
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

        barcodeDetector.setProcessor( new Detector.Processor<Barcode>() {
            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> qr_codes = detections.getDetectedItems();
                if(qr_codes.size() != 0)
                {
                    txtResult.post( new Runnable() {
                        @Override
                        public void run() {
                            // create vibrate
                            Vibrator vibrator = (Vibrator)getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE );
                            vibrator.vibrate( 1000 );
                            txtResult.setText( qr_codes.valueAt( 0 ).displayValue );
                        }
                    } );
                }
            }
        } );
    }
}
