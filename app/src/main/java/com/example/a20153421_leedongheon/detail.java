package com.example.a20153421_leedongheon;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class detail extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_DELETE_PACKAGES = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        setTitle("Detail Information");

        int permissionCheck = ContextCompat.checkSelfPermission(detail.this, Manifest.permission.REQUEST_DELETE_PACKAGES);

        if(permissionCheck != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(detail.this   ,
                    new String[]{Manifest.permission.REQUEST_DELETE_PACKAGES},
                    MY_PERMISSIONS_REQUEST_DELETE_PACKAGES);
        }

        final Intent intent = getIntent();
        String appName = intent.getStringExtra("AppName");
        String description = intent.getStringExtra("Description");
        Bitmap image = (Bitmap) intent.getExtras().get("Image");
        String lastTimeUsed = intent.getStringExtra("LastTimeUsed");
        String totalTimeUsed = intent.getStringExtra("TotalTimeUsed");
        String lastTimeVisible = intent.getStringExtra("LastTimeVisible");

        ImageView imageV = findViewById(R.id.image);
        TextView mainV = findViewById(R.id.textView1);
        TextView subV = findViewById(R.id.textView2);
        TextView lastV = findViewById(R.id.lastTimeUsed);
        TextView totalV = findViewById(R.id.totalTimeUsed);
        TextView lastVV = findViewById(R.id.lastVisibleTime);


        imageV.setImageBitmap(image);
        mainV.setText(appName);
        subV.setText(description);
        lastV.setText(lastV.getText() + " : "+lastTimeUsed);
        totalV.setText(totalV.getText() + " : "+totalTimeUsed);
        lastVV.setText(lastVV.getText() + " : "+lastTimeVisible);


        Button deleteBtn = findViewById(R.id.deleteButton);

        if(intent.getStringExtra("PackageName") == null) deleteBtn.setEnabled(false);

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri packageUri = Uri.parse("package:"+intent.getStringExtra("PackageName"));
                Intent uninstallIntent = new Intent(Intent.ACTION_DELETE,packageUri);
                startActivity(uninstallIntent);
            }
        });

    }
}
