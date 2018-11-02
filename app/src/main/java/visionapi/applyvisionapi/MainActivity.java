package visionapi.applyvisionapi;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button textBtn; // button call text recognition activity
    Button qr_codeBtn; // Button call QR Code activity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );

        textBtn = findViewById( R.id.btn_text );
        qr_codeBtn = findViewById( R.id.btn_qr_code );

        textBtn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent textIntent = new Intent( MainActivity.this, TextRecognition.class );
                startActivity( textIntent );
            }
        } );

        qr_codeBtn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent qr_codeIntent = new Intent( MainActivity.this,QRCode.class );
                startActivity( qr_codeIntent );
            }
        } );

    }
}
