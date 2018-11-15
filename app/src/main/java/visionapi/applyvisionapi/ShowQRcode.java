package visionapi.applyvisionapi;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class ShowQRcode extends AppCompatActivity {

    ImageView showQRcode;
    TextView textView;
    Button returnOcr;
    Button returnQR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_show_qrcode );

        textView = (TextView) findViewById( R.id.txtv_ocr );
        showQRcode = (ImageView)findViewById( R.id.qr_code );
        returnOcr = (Button) findViewById( R.id.btn_text );
        returnQR = (Button) findViewById( R.id.btn_qrcode );

        String text_reg = getIntent().getStringExtra( "text" );
        Bitmap qr_code_gen = getIntent().getParcelableExtra( "qr_code" );

        textView.setText( text_reg );
        showQRcode.setImageBitmap( qr_code_gen );

        returnOcr.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent( ShowQRcode.this,TextRecognition.class );
                startActivity( intent );
            }
        } );

        returnQR.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent( ShowQRcode.this, QRCode.class);
                startActivity( intent );
            }
        } );

    }
}
