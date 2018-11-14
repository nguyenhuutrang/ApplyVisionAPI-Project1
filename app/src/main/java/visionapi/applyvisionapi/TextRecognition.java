package visionapi.applyvisionapi;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.samples.vision.ocrreader.OcrDetectorProcessor;
import com.google.android.gms.samples.vision.ocrreader.OcrGraphic;
import com.google.android.gms.samples.vision.ocrreader.ui.camera.CameraSource;
import com.google.android.gms.samples.vision.ocrreader.ui.camera.CameraSourcePreview;
import com.google.android.gms.samples.vision.ocrreader.ui.camera.GraphicOverlay;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.IOException;
import java.util.Locale;

public class TextRecognition extends AppCompatActivity {
    private static final String TAG = "OcrCaptureActivity";

    // Intent request code to handle updating play services if needed.
    private static final int RC_HANDLE_GMS = 9001;

    // Permission request codes need to be < 256
    private static final int RC_HANDLE_CAMERA_PERM = 2;

    // Constants used to pass extra data in the intent
    public static final String AutoFocus = "AutoFocus";
    public static final String UseFlash = "UseFlash";
    public static final String TextBlockObject = "String";

    private CameraSource cameraSource;
    private CameraSourcePreview preview;
    private GraphicOverlay<OcrGraphic> graphicOverlay;

    // Helper objects for detecting taps and pinches.
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;

    // A TextToSpeech engine for speaking a String value.
    private TextToSpeech tts;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_text_recognition );

        preview = (CameraSourcePreview) findViewById( R.id.preview );
        graphicOverlay = (GraphicOverlay<OcrGraphic>) findViewById( R.id.graphicOverlay );

        // Set good default for capturing text
        boolean autoFocus = true;
        boolean useFlash = false;

        // Check for the camera permission before accessing the camera.  If the
        // permission is not granted yet, request permission.
        int rc = ActivityCompat.checkSelfPermission( this, Manifest.permission.CAMERA );
        if (rc == PackageManager.PERMISSION_GRANTED) {
            createCameraSource( autoFocus, useFlash );
        }else {
            requestCameraPermission();
        }

        gestureDetector = new GestureDetector( this,new CaptureGestureListener());
        scaleGestureDetector = new ScaleGestureDetector( this, new ScaleListener() );

        // Set up the Text To Speech engine.
        TextToSpeech.OnInitListener listener = new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(final int status) {
                        if (status == TextToSpeech.SUCCESS) {
                            Log.d("OnInitListener", "Text to speech engine started successfully.");
                            tts.setLanguage(Locale.US);
                        } else {
                            Log.d("OnInitListener", "Error starting the text to speech engine.");
                        }
                    }
                };
        tts = new TextToSpeech(this.getApplicationContext(), listener);

    }

    private void requestCameraPermission() {
        Log.w( TAG, "Camera permission is not granted. Requesting permission" );
        final String[] permission = new String[]{Manifest.permission.CAMERA};
        if(!ActivityCompat.shouldShowRequestPermissionRationale( this,Manifest.permission.CAMERA )){
            ActivityCompat.requestPermissions( this,permission,RC_HANDLE_CAMERA_PERM );
            return;
        }

        final Activity thisActivity = this;
        View.OnClickListener listener = new View.OnClickListener(){
            @Override
            public void onClick(View view){
                ActivityCompat.requestPermissions( thisActivity,permission,RC_HANDLE_CAMERA_PERM );
            }
        };
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        boolean b = scaleGestureDetector.onTouchEvent(e);

        boolean c = gestureDetector.onTouchEvent(e);

        return b || c || super.onTouchEvent(e);
    }

    @SuppressLint("InlineApi")
    private void createCameraSource(boolean autoFocus, boolean useFlash) {
        Context context = getApplicationContext();
        TextRecognizer textRecognizer = new TextRecognizer.Builder( context ).build();
        textRecognizer.setProcessor( new OcrDetectorProcessor( graphicOverlay ) );

        if (!textRecognizer.isOperational()) {
            Log.w( TAG, "Detector dependencies are not yet available." );
        }

        cameraSource = new CameraSource.Builder( getApplicationContext(), textRecognizer )
                .setFacing( CameraSource.CAMERA_FACING_BACK )
                .setRequestedPreviewSize( 1280, 1024 )
                .setRequestedFps( 2.0f )
                .setFlashMode(useFlash ? Camera.Parameters.FLASH_MODE_TORCH : null)
                .setFocusMode(autoFocus ? Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO : null)
                .build();

    }

    /**
     * Restarts the camera.
     */
    @Override
    protected void onResume() {
        super.onResume();
        startCameraSource();
    }


    /**
     * Stops the camera.
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (preview != null) {
            preview.stop();
        }
    }

    /**
     * Releases the resources associated with the camera source, the associated detectors, and the
     * rest of the processing pipeline.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (preview != null) {
            preview.release();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            Log.d( TAG, "Got unexpected permission result: " + requestCode );
            super.onRequestPermissionsResult( requestCode, permissions, grantResults );
            return;
        }

        Log.e( TAG, "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)") );

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder( this );
        builder.setTitle( "Multitracker sample" )
                .setMessage( "No camera permission" )
                .setPositiveButton( "ok", listener )
                .show();
    }

    private void startCameraSource() throws SecurityException{
        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable( getApplicationContext() );
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg = GoogleApiAvailability.getInstance().getErrorDialog( this, code, RC_HANDLE_GMS );
            dlg.show();
        }

        if (cameraSource != null) {
            try {
                preview.start( cameraSource, graphicOverlay );
            } catch ( IOException e){
                Log.e( TAG, "Unable to start camera source.",e );
                cameraSource.release();
                cameraSource = null;
            }
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private boolean onTap(float rawX, float rawY) {
        OcrGraphic graphic = graphicOverlay.getGraphicAtLocation(rawX, rawY);
        TextBlock text = null;
        if (graphic != null) {
            text = graphic.getTextBlock();
            if (text != null && text.getValue() != null) {
                Log.d(TAG, "text data is being spoken! " + text.getValue());
                // Speak the string.
                tts.speak(text.getValue(), TextToSpeech.QUEUE_ADD, null, "DEFAULT");

            }
            else {
                Log.d(TAG, "text data is null");
            }
        }
        else {
            Log.d(TAG,"no text detected");
        }
        return text != null;
    }

    private class CaptureGestureListener extends GestureDetector.SimpleOnGestureListener {

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            return onTap(e.getRawX(), e.getRawY()) || super.onSingleTapConfirmed(e);
        }
    }

    private class ScaleListener implements ScaleGestureDetector.OnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            return false;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            if (cameraSource != null) {
                cameraSource.doZoom( detector.getScaleFactor() );
            }
        }
    }
}