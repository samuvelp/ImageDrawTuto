package com.pandian.samuvel.imagedrawtuto;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.OutputStream;

import static com.pandian.samuvel.imagedrawtuto.R.id.resultImage;

public class MainActivity extends AppCompatActivity {
    private Button loadImageButton;
    private Button saveImageButton;
    private ImageView resultImageView;
    private Bitmap mBitmap;
    private static final int REQ_IMAGE =1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadImageButton = (Button) findViewById(R.id.loadimageButton);
        saveImageButton = (Button) findViewById(R.id.saveImageButton);
        resultImageView = (ImageView) findViewById(resultImage);


        Bundle bundle = getIntent().getExtras();
        try {
            if (bundle.get("imageBytes") != null) {
                loadImage();
            }
        }
        catch (NullPointerException e){}


        loadImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQ_IMAGE);
            }
        });

        saveImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mBitmap!=null){
                    saveBitmap(mBitmap);
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            switch (requestCode){
                case REQ_IMAGE:
                    Uri imageUri = data.getData();
                    Bitmap bitmap = null;
                    try {
                        bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    try {
                        saveImageInPreference(bitmap,imageUri);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                    Intent intent = new Intent(this,DrawingActivity.class);
                    intent.putExtra("imageUri",imageUri);
                    startActivity(intent);
                    break;
            }
        }
    }
    private void saveImageInPreference(Bitmap bitmap,Uri mSource) throws FileNotFoundException {

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        bitmap = BitmapFactory.decodeStream(
                getContentResolver().openInputStream(mSource),null,options);
        if(options.outHeight>2560 || options.outWidth>1440) {
            options.inSampleSize = 4;
        }
        else options.inSampleSize = 1;
        options.inJustDecodeBounds = false;
        bitmap = BitmapFactory.decodeStream(
                getContentResolver().openInputStream(mSource),null,options);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,15,byteArrayOutputStream);
        byte[] originalImageBytes = byteArrayOutputStream.toByteArray();
        String encodedImage = Base64.encodeToString(originalImageBytes,Base64.DEFAULT);
        SharedPreferences sharedPreferences = getSharedPreferences("OriginalImage",MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("OriginalImage",encodedImage);
        editor.apply();
    }
    private void loadImage(){
        Bundle bundle = getIntent().getExtras();
        byte[] bytes = bundle.getByteArray("imageBytes");
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
        mBitmap = bitmap;
        resultImageView.setImageBitmap(bitmap);

    }
    private void saveBitmap(Bitmap bm) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        Uri imageFileUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);
        try {
            OutputStream imageFileOs = getContentResolver().openOutputStream(imageFileUri);
            bm.compress(Bitmap.CompressFormat.JPEG, 100, imageFileOs);

            Toast.makeText(MainActivity.this,
                    "Image Saved",
                    Toast.LENGTH_LONG).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this,
                    "Something wrong: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }
}
