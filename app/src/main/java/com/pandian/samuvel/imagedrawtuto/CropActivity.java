package com.pandian.samuvel.imagedrawtuto;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class CropActivity extends AppCompatActivity {
    private Button mCropImageButton;
    private CropImageView mCropImageView;
    private Bitmap mBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);

        mCropImageButton = (Button) findViewById(R.id.cropImageButton);
        mCropImageView = (CropImageView) findViewById(R.id.cropImageView);

        loadImage();

        mCropImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAlertDialog();

            }
        });

    }
    private void loadImage(){
        Bundle bundle = getIntent().getExtras();
        byte[] imageBytes = bundle.getByteArray("imageBytesCrop");
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        mBitmap = BitmapFactory.decodeByteArray(imageBytes,0,imageBytes.length);

        mBitmap.compress(Bitmap.CompressFormat.JPEG, 15, byteArrayOutputStream);
        mBitmap = BitmapFactory.decodeStream(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
        mCropImageView.setImageBitmap(mBitmap);//setting image to cropping image view
    }
    private void sendCroppedImage(){
        mBitmap = mCropImageView.getCroppedImage();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        mBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] imageBytes = byteArrayOutputStream.toByteArray();
        Intent intent = new Intent(CropActivity.this,DrawingActivity.class);
        intent.putExtra("imageBytesCropped", imageBytes);
        startActivity(intent);
    }
    private void openAlertDialog(){
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        //Yes button clicked
                        sendCroppedImage();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(CropActivity.this);
        builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }
}
