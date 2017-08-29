package com.pandian.samuvel.imagedrawtuto;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

public class ImageResultActivity extends AppCompatActivity {
    private Button loadImageButton;
    private Button saveImageButton;
    private ImageView resultImageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_result);

        loadImageButton = (Button) findViewById(R.id.loadimageButton);
        saveImageButton = (Button) findViewById(R.id.saveimageButton);
        resultImageView = (ImageView) findViewById(R.id.resultImage);


    }
}
