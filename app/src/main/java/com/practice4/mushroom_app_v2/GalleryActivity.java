package com.practice4.mushroom_app_v2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class GalleryActivity extends AppCompatActivity {

    ImageView img;
    Button backBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        // ImageView
        img=findViewById(R.id.img);
        backBtn=findViewById(R.id.back);
        Intent receivedIntent= getIntent();
        if(receivedIntent!=null)
        {
            String sharedData= receivedIntent.getStringExtra("image");
            Toast.makeText(this,sharedData,Toast.LENGTH_SHORT).show();
            new DownloadImageTask(img)
                    .execute(sharedData);
        }
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // go to main activity
                Intent mainView= new Intent(GalleryActivity.this,MainActivity.class);
                startActivity(mainView);

            }
        });

    }
}