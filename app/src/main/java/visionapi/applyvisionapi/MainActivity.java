package visionapi.applyvisionapi;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button textBtn; // button text recognition
    Button faceBtn; // button face detection

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );

        textBtn = (Button)findViewById( R.id.btn_text );
        faceBtn = (Button)findViewById( R.id.btn_face );

        textBtn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent textIntent = new Intent(MainActivity.this,TextRecognition.class);
                startActivity( textIntent );
            }
        } );

        faceBtn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent faceIntent = new Intent(MainActivity.this,FaceDetection.class);
                startActivity( faceIntent );
            }
        } );
    }
}
