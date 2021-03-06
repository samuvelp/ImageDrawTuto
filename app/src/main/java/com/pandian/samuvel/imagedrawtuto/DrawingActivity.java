package com.pandian.samuvel.imagedrawtuto;


import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;

public class DrawingActivity extends AppCompatActivity {


    private Button mOkButton;
    private Button mUndoButton;
    private Button mWhiteColorButton;
    private Button mRedColorButton;
    private Button mOrangeColorButton;
    private Button mYellowColorButton;
    private Button mBrownColorButton;
    private Button mBlueColorButton;
    private Button mGreenColorButton;
    private Button mCancelButton;
    private Button mCropButton;
    private ImageView mImageView;
    private Uri mSource;
    private Bitmap mBitmap;
    private Bitmap mInitialImage;
    private Canvas mCanvasMaster;
    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;
    private Paint mPaint;
    private Path mPath;
    private Bitmap tempBitmap;
    private LinearLayout mColorIndicator;
    private GradientDrawable gradientDrawable;
    private int whiteColor;
    private int redColor;
    private int orangeColor;
    private int yellowColor;
    private int brownColor;
    private int blueColor;
    private int greenColor;
    private int currentColor=-65536;//red color int value

    private ArrayList<Path> mPathList = new ArrayList<>();
    private ArrayList<Paint> mPaintList = new ArrayList<>();
    private HashMap<Path,Integer> colorMaps = new HashMap<>();
    final int REQ_IMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawing);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        mOkButton = (Button) findViewById(R.id.okButton);
        mUndoButton = (Button) findViewById(R.id.undoButton);
        mImageView = (ImageView) findViewById(R.id.imageView);
        mWhiteColorButton = (Button) findViewById(R.id.whiteColorButton);
        mRedColorButton = (Button) findViewById(R.id.redColorButton);
        mOrangeColorButton = (Button) findViewById(R.id.orangeColorButton);
        mYellowColorButton = (Button) findViewById(R.id.yellowColorButton);
        mBrownColorButton = (Button) findViewById(R.id.brownColorButton);
        mBlueColorButton = (Button) findViewById(R.id.blueColorButton);
        mGreenColorButton = (Button) findViewById(R.id.greenColorButton);
        mCancelButton = (Button) findViewById(R.id.cancelButton);
        mCropButton = (Button) findViewById(R.id.cropButton);
        mColorIndicator = (LinearLayout) findViewById(R.id.colorIndicator);

        whiteColor = ContextCompat.getColor(getApplicationContext(),R.color.whiteColor);
        redColor = ContextCompat.getColor(getApplicationContext(),R.color.redColor);
        orangeColor = ContextCompat.getColor(getApplicationContext(),R.color.orangeColor);
        yellowColor = ContextCompat.getColor(getApplicationContext(),R.color.yellowColor);
        brownColor = ContextCompat.getColor(getApplicationContext(),R.color.brownColor);
        blueColor = ContextCompat.getColor(getApplicationContext(),R.color.blueColor);
        greenColor = ContextCompat.getColor(getApplicationContext(),R.color.greenColor);
        gradientDrawable = (GradientDrawable)mColorIndicator.getBackground().getCurrent();

        mPath = new Path();
        mCanvasMaster = new Canvas();

        startDefaultPaint();

        try{
            loadCroppedImage();
        }catch (NullPointerException e){}
        try {
            openBitmapReduced();
        }catch (NullPointerException e){}

        mOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBitmap != null) {
                    convertBitmapToByte(mBitmap);
                }
            }
        });

        mUndoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(mPathList.size()>0) {
                    mPathList.remove(mPathList.size() - 1);
                    mCanvasMaster.drawBitmap(tempBitmap,0,0,null);
                    mPath.reset();
                    drawOnProjectedBitMap();
                    mImageView.invalidate();
                }

            }
        });
        mCropButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mBitmap != null) {
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                    byte[] imageBytesCrop = byteArrayOutputStream.toByteArray();
                    Intent intent = new Intent(DrawingActivity.this, CropActivity.class);
                   // intent.putExtra("imageBytesCrop", imageBytesCrop);
                    startActivity(intent);
                }
            }
        });
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAlertDialog();
            }
        });
        mWhiteColorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Paint whitePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                whitePaint.setStyle(Paint.Style.STROKE);
                whitePaint.setColor(whiteColor);
                whitePaint.setStrokeJoin(Paint.Join.ROUND);
                whitePaint.setStrokeCap(Paint.Cap.ROUND);
                whitePaint.setStrokeWidth(10);
                mPaintList.add(whitePaint);
                currentColor = whiteColor;
                drawOnProjectedBitMap();

                gradientDrawable.setColor(whiteColor);

            }
        });
        mRedColorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Paint redPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                redPaint.setStyle(Paint.Style.STROKE);
                redPaint.setColor(redColor);
                redPaint.setStrokeJoin(Paint.Join.ROUND);
                redPaint.setStrokeCap(Paint.Cap.ROUND);
                redPaint.setStrokeWidth(10);
                mPaintList.add(redPaint);
                currentColor = redColor;
                 drawOnProjectedBitMap();
                gradientDrawable.setColor(redColor);
            }
        });
        mOrangeColorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Paint orangePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                orangePaint.setStyle(Paint.Style.STROKE);
                orangePaint.setColor(orangeColor);
                orangePaint.setStrokeJoin(Paint.Join.ROUND);
                orangePaint.setStrokeCap(Paint.Cap.ROUND);
                orangePaint.setStrokeWidth(10);
                mPaintList.add(orangePaint);
                currentColor = orangeColor;
                drawOnProjectedBitMap();
                gradientDrawable.setColor(orangeColor);
            }
        });
        mYellowColorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Paint yellowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                yellowPaint.setStyle(Paint.Style.STROKE);
                yellowPaint.setColor(yellowColor);
                yellowPaint.setStrokeJoin(Paint.Join.ROUND);
                yellowPaint.setStrokeCap(Paint.Cap.ROUND);
                yellowPaint.setStrokeWidth(10);
                mPaintList.add(yellowPaint);
                currentColor = yellowColor;
                drawOnProjectedBitMap();
                gradientDrawable.setColor(yellowColor);
            }
        });
        mBrownColorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Paint brownPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                brownPaint.setStyle(Paint.Style.STROKE);
                brownPaint.setColor(brownColor);
                brownPaint.setStrokeJoin(Paint.Join.ROUND);
                brownPaint.setStrokeCap(Paint.Cap.ROUND);
                brownPaint.setStrokeWidth(10);
                mPaintList.add(brownPaint);
                currentColor = brownColor;
                drawOnProjectedBitMap();
                gradientDrawable.setColor(brownColor);
            }
        });
        mBlueColorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Paint bluePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                bluePaint.setStyle(Paint.Style.STROKE);
                bluePaint.setColor(blueColor);
                bluePaint.setStrokeJoin(Paint.Join.ROUND);
                bluePaint.setStrokeCap(Paint.Cap.ROUND);
                bluePaint.setStrokeWidth(10);
                mPaintList.add(bluePaint);
                currentColor = blueColor;
                drawOnProjectedBitMap();
                gradientDrawable.setColor(blueColor);
            }
        });
        mGreenColorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Paint greenPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                greenPaint.setStyle(Paint.Style.STROKE);
                greenPaint.setColor(greenColor);
                greenPaint.setStrokeJoin(Paint.Join.ROUND);
                greenPaint.setStrokeCap(Paint.Cap.ROUND);
                greenPaint.setStrokeWidth(10);
                mPaintList.add(greenPaint);
                currentColor = greenColor;
                drawOnProjectedBitMap();
                gradientDrawable.setColor(greenColor);
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
        for(Path path : mPathList){
            mPaint.setColor(colorMaps.get(path));
            mCanvasMaster.drawPath(path,mPaint);
        }
            Paint paint = mPaintList.get(mPaintList.size()-1);
            mCanvasMaster.drawPath(mPath,paint);
    }
    private void touch_start(ImageView iv, Bitmap bm,float x, float y) {
        //mPath.reset();
        float ratioWidth=0;
        float ratioHeight=0;
        if (x < 0 || y < 0 || x > iv.getWidth() || y > iv.getHeight()) {
            //outside ImageView
            return;
        }
        else {
            ratioWidth = (float) bm.getWidth() / (float) iv.getWidth();
            ratioHeight = (float) bm.getHeight() / (float) iv.getHeight();
            Paint lastPaintObj = mPaintList.get(mPaintList.size()-1);
            lastPaintObj.setStrokeWidth(ratioWidth*10);
            mPath.moveTo(x * ratioWidth, y * ratioHeight);
            colorMaps.put(mPath,currentColor);
            //drawOnProjectedBitMap(lastPaintObj);
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
            Paint lastPaintObj = mPaintList.get(mPaintList.size()-1);
            lastPaintObj.setStrokeWidth(ratioWidth*10);
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
        Paint lastPaintObj = mPaintList.get(mPaintList.size()-1);
        mCanvasMaster.drawPath(mPath, lastPaintObj);
        // kill this so we don't double draw
       // drawOnProjectedBitMap(lastPaintObj);
        mPathList.add(mPath);
        mPath = new Path();
    }


    private void startDefaultPaint(){
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.RED);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(10);
        mPaintList.add(mPaint);
    }


    private void openBitmapReduced(){
        Bundle bundle =getIntent().getExtras();
        mSource = (Uri) bundle.get("imageUri");
        try {
            //tempBitmap is Immutable bitmap,
            //cannot be passed to Canvas constructor
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            tempBitmap = BitmapFactory.decodeStream(
                    getContentResolver().openInputStream(mSource),null,options);
            if(options.outHeight>2560 || options.outWidth>1440) {
                options.inSampleSize = 4;
            }
            else options.inSampleSize = 1;
            options.inJustDecodeBounds = false;

            tempBitmap = BitmapFactory.decodeStream(
                    getContentResolver().openInputStream(mSource),null,options);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            tempBitmap.compress(Bitmap.CompressFormat.JPEG,15,byteArrayOutputStream);
            tempBitmap = BitmapFactory.decodeStream(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));

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
    }

    private void loadCroppedImage(){

        Bundle bundle = getIntent().getExtras();
        byte[] imageBytes = bundle.getByteArray("imageBytesCropped");
        tempBitmap = BitmapFactory.decodeByteArray(imageBytes,0,imageBytes.length);
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
        drawOnProjectedBitMap();

    }
    private void convertBitmapToByte(Bitmap bm) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100,outputStream);
        byte[] bytes = outputStream.toByteArray();

        Intent intent = new Intent(this,MainActivity.class);
        intent.putExtra("imageBytes",bytes);
        startActivity(intent);
    }

    private void openAlertDialog(){
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        //Yes button clicked
                        Intent intent = new Intent(DrawingActivity.this,MainActivity.class);
                        startActivity(intent);
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(DrawingActivity.this);
        builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        openAlertDialog();
    }

}
