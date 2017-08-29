package com.pandian.samuvel.imagedrawtuto;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;



import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedList;

public class MainActivity extends AppCompatActivity {

    private Button mLoadImageButton;
    private Button mSaveImageButton;
    private Button mUndoButton;
    private Button mWhiteColorButton;
    private Button mRedColorButton;
    private Button mOrangeColorButton;
    private Button mYellowColorButton;
    private Button mBrownColorButton;
    private Button mBlueColorButton;
    private Button mGreenColorButton;
    private ImageView mImageView;
    private Uri mSource;
    private Bitmap mBitmap;
    private Canvas mCanvasMaster;
    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;
    private Paint mPaint;
    private Path mPath;
    private Bitmap tempBitmap;
    private ArrayList<Path> mPaths = new ArrayList<>();
    final int REQ_IMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
        mLoadImageButton = (Button) findViewById(R.id.loadimageButton);
        mSaveImageButton = (Button) findViewById(R.id.saveimageButton);
        mUndoButton = (Button) findViewById(R.id.undoButton);
        mImageView = (ImageView) findViewById(R.id.imageView);
        mWhiteColorButton = (Button) findViewById(R.id.whiteColorButton);

        mPath = new Path();
        mCanvasMaster = new Canvas();
        //mPaths.add(mPath);

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.RED);
        mPaint.setStrokeJoin(Paint.Join.BEVEL);
        mPaint.setStrokeWidth(10);

        mLoadImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                Intent intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQ_IMAGE);
            }
        });

        mSaveImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBitmap != null) {
                    saveBitmap(mBitmap);
                }
            }
        });
        mUndoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mPaths.size()>0) {
                    mPaths.remove(mPaths.size() - 1);
                    mCanvasMaster.drawBitmap(tempBitmap,0,0,null);
                    mPath.reset();
                    drawOnProjectedBitMap();
                    mImageView.invalidate();
                }

            }
        });
        mWhiteColorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPaint.setColor(Color.WHITE);
            }
        });
        mImageView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                int action = event.getAction();
                int x = (int) event.getX();
                int y = (int) event.getY();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        touch_start((ImageView) v, mBitmap,x,y);
                        mImageView.invalidate();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        touch_move((ImageView) v, mBitmap,x,y);
                        mImageView.invalidate();
                        break;
                    case MotionEvent.ACTION_UP:
                        touch_up();
                        mImageView.invalidate();
                        break;
                }
                return true;
            }
        });

    }

    private void drawOnProjectedBitMap() {
        for(Path path : mPaths){
            mCanvasMaster.drawPath(path,mPaint);
        }
            mCanvasMaster.drawPath(mPath,mPaint);
    }
    private void touch_start(ImageView iv, Bitmap bm,float x, float y) {
       // mPath.reset();
        float ratioWidth=0;
        float ratioHeight=0;
        if (x < 0 || y < 0 || x > iv.getWidth() || y > iv.getHeight()) {
            //outside ImageView
            return;
        }
        else {
            ratioWidth = (float) bm.getWidth() / (float) iv.getWidth();
            ratioHeight = (float) bm.getHeight() / (float) iv.getHeight();
            mPaint.setStrokeWidth(ratioWidth*10);
            mPath.moveTo(x * ratioWidth, y * ratioHeight);
            drawOnProjectedBitMap();
        }
        mX = x * ratioWidth;
        mY = y * ratioHeight;
    }
    private void touch_move(ImageView iv, Bitmap bm,float x, float y) {
        float ratioWidth=0;
        float ratioHeight=0;

        if (x < 0 || y < 0 || x > iv.getWidth() || y > iv.getHeight()) {
            //outside ImageView
            return;
        }
        else {
            ratioWidth = (float) bm.getWidth() / (float) iv.getWidth();
            ratioHeight = (float) bm.getHeight() / (float) iv.getHeight();
            mPaint.setStrokeWidth(ratioWidth*10);
            x = x*ratioWidth;
            y = y*ratioHeight;
            float dx = Math.abs(x - mX);
            float dy = Math.abs(y - mY);
            if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
                mX = x;
                mY = y;
                drawOnProjectedBitMap();
            }
        }
    }
    private void touch_up() {
        mPath.lineTo(mX, mY);
        // commit the path to our offscreen
        mCanvasMaster.drawPath(mPath, mPaint);
        // kill this so we don't double draw
        mPaths.add(mPath);
        mPath = new Path();
        drawOnProjectedBitMap();

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQ_IMAGE:
                    mSource = data.getData();

                    try {
                        //tempBitmap is Immutable bitmap,
                        //cannot be passed to Canvas constructor
                        tempBitmap = BitmapFactory.decodeStream(
                                getContentResolver().openInputStream(mSource));

                        Bitmap.Config config;
                        if (tempBitmap.getConfig() != null) {
                            config = tempBitmap.getConfig();
                        } else {
                            config = Bitmap.Config.ARGB_8888;
                        }

                        //mBitmap is Mutable bitmap
                        mBitmap = Bitmap.createBitmap(
                                tempBitmap.getWidth(),
                                tempBitmap.getHeight(),
                                config);

                        mCanvasMaster = new Canvas(mBitmap);
                        mCanvasMaster.drawBitmap(tempBitmap, 0, 0, null);

                        mImageView.setImageBitmap(mBitmap);
                    } catch (FileNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    break;
            }
        }
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
