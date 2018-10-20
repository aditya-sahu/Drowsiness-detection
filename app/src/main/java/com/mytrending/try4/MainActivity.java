package com.mytrending.try4;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Size;
import android.util.SparseArray;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.Landmark;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileNotFoundException;

public class MainActivity extends AppCompatActivity {

    private String cameraId;
    private CameraManager cameraManager;
    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSession;
    private Size imageDimension;
    private ImageReader imageReader;

    private static final int REQUESR_CAMERA_PERMISSION = 200;


    TextureView textureView;
    TextView txtSampleDesc;
    private FaceDetector detector;
    Bitmap editedBitmap;
    int img;
    Size size;
    Handler backgroundHandler;
    HandlerThread handlerThread;


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textureView = (TextureView) findViewById(R.id.mainView);
    }

    private void openBackgroundHandler() {
        HandlerThread handlerThread = new HandlerThread("Camera_app");
        handlerThread.start();
        backgroundHandler = new Handler(handlerThread.getLooper());
    }
    private void closeBackgroundHandler() {
            handlerThread.quit();
            handlerThread = null;
            backgroundHandler = null;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setupCamera() {
        cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        try {
            String[] cameraIds = cameraManager.getCameraIdList();
            for (String cameraId : cameraIds) {
                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
                if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == CameraMetadata.LENS_FACING_FRONT) {
                    this.cameraId = cameraId;
                    StreamConfigurationMap streamConfigurationMap = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                    size = streamConfigurationMap.getOutputSizes(SurfaceTexture.class)[0];

                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener()

        {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                openCamera();
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }

            private void openCamera() {

            }
        });












        img = R.drawable.no;
        detector = new FaceDetector.Builder(

                getApplicationContext())
                .setTrackingEnabled(false)
                .setLandmarkType(FaceDetector.ALL_CLASSIFICATIONS)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build();

        initViews();
    }
    private void initViews() {
        TextureView layoutmain = (TextureView) findViewById(R.id.mainView);
        txtSampleDesc = (TextView) findViewById(R.id.txtSampleDescription3);
        processImage(img);

    }

    private void processImage(int image) {

        Bitmap bitmap = decodeBitmapImage(image);
        if (detector.isOperational() && bitmap != null) {
            editedBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap
                    .getHeight(), bitmap.getConfig());
            float scale = getResources().getDisplayMetrics().density;
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(Color.RED);
            paint.setTextSize((int) (16 * scale));
            paint.setShadowLayer(1f, 0f, 1f, Color.WHITE);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(6f);
            Canvas canvas = new Canvas(editedBitmap);
            canvas.drawBitmap(bitmap, 0, 0, paint);
            Frame frame = new Frame.Builder().setBitmap(editedBitmap).build();
            SparseArray<Face> faces = detector.detect(frame);
            txtSampleDesc.setText(null);

            for (int index = 0; index < faces.size(); ++index) {
                Face face = faces.valueAt(index);
                canvas.drawRect(
                        face.getPosition().x,
                        face.getPosition().y,
                        face.getPosition().x + face.getWidth(),
                        face.getPosition().y + face.getHeight(), paint);

                canvas.drawText("Addy ", face.getPosition().x, face.getPosition().y, paint);
                txtSampleDesc.setText(txtSampleDesc.getText() + "Left Eye Is Open Probability: " + " " + face.getIsLeftEyeOpenProbability() + "\n");
                txtSampleDesc.setText(txtSampleDesc.getText() + "Right Eye Is Open Probability: " + " " + face.getIsRightEyeOpenProbability() + "\n\n");
                txtSampleDesc.setText(txtSampleDesc.getText() + "EULER Y" + " " + face.getEulerY() + "\n\n");
                txtSampleDesc.setText(txtSampleDesc.getText() + "EULER Z" + " " + face.getEulerZ() + "\n\n");
                txtSampleDesc.setText(txtSampleDesc.getText() + "HEIGHT: " + " " + face.getHeight() + "\n\n");
                txtSampleDesc.setText(txtSampleDesc.getText() + "WIDTH: " + " " + face.getWidth() + "\n\n");
                if((face.getIsLeftEyeOpenProbability()<0.4)&&(face.getIsRightEyeOpenProbability()<0.4))
                    Toast.makeText(this, "WAKE UP BITCH", Toast.LENGTH_SHORT).show();
                for (Landmark landmark : face.getLandmarks()) {
                    int cx = (int) (landmark.getPosition().x);
                    int cy = (int) (landmark.getPosition().y);
                    canvas.drawCircle(cx, cy, 8, paint);
                }


            }

            if (faces.size() == 0) {
                txtSampleDesc.setText("Scan Failed: Found nothing to scan");
            } else {
                imageView.setImageBitmap(editedBitmap);
                txtSampleDesc.setText(txtSampleDesc.getText() + "No of Faces Detected: " + " " + String.valueOf(faces.size()));
            }
        } else {
            txtSampleDesc.setText("Could not set up the detector!");
        }
    }

    private Bitmap decodeBitmapImage(int image) {
        int targetW = 300;
        int targetH = 300;
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;

        BitmapFactory.decodeResource(getResources(), image,
                bmOptions);

        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        return BitmapFactory.decodeResource(getResources(), image,
                bmOptions);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        detector.release();
    }
}

