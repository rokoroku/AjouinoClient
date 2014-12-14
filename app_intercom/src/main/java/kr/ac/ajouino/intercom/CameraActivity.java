package kr.ac.ajouino.intercom;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import kr.ac.ajouino.intercom.model.Event;
import kr.ac.ajouino.intercom.util.JmDnsUtils;
import kr.ac.ajouino.intercom.util.EventSender;


// SurfaceView를 이용해 미리보기 화면을 만든 후 게스트 사진 찍는 Class
public class CameraActivity extends Activity implements Camera.PictureCallback {

    private Camera mCamera;
    private CameraPreview mPreview;
    private ProgressBar mProgressBar;
    private CountDownTimer timer;

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_camera);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        // Call Thread 호출
        // callThread ct = new callThread();
        // ct.start();

    }

    @Override
    protected void onPause() {
        super.onPause();
        // Create our Preview view and set it as the content of our activity.
        if (mPreview != null) {
            FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
            preview.removeView(mPreview);
        }
        releaseCamera();              // release the camera immediately on pause event
    }

    @Override
    protected void onResume() {
        super.onStart();
        // Create an instance of Camera
        mCamera = getCameraInstance();

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);

        // Button 누르면 캡쳐
        Button captureButton = (Button) findViewById(R.id.button_capture);
        captureButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mProgressBar.setVisibility(View.VISIBLE);
                TextView text = (TextView) findViewById(R.id.text);
                text.setText("방문 기록을 남기는 중입니다...");
                if(timer != null) {
                    timer.cancel();
                }
                mCamera.takePicture(null, null, CameraActivity.this);
            }
        });

        // Timer 30 초 설정
        if(timer == null) {
            timer = new CountDownTimer(60 * 1000, 1000) {

                int value = 0;
                final TextView time = (TextView) findViewById(R.id.time);

                @Override
                public void onTick(long millisUntilFinished) {
                    value++;
                    time.setText("0:" + Integer.toString(value));
                }

                @Override
                public void onFinish() {
                    Log.v("TIMER", "1분 지났습니다.");
                    finish();
                }
            };
            timer.start();
        }

    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {

        File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
        if (pictureFile == null) {
            Log.d(this.getClass().getSimpleName(), "Error creating media file, check storage permissions");
            return;
        }

        try {
            //이미지 저장
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

            //삼성폰의 경우, 이미지 회전
            if (Build.MANUFACTURER.contains("samsung")) {
                bitmap = rotateImage(bitmap, 270);
            }

            //이미지 리사이즈
            bitmap = resizeImage(bitmap, 600, 800);

            //이미지 OutputStream 변환 후 저장
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 75, stream);
            data = stream.toByteArray();

            FileOutputStream fos = new FileOutputStream(pictureFile);
            fos.write(data);
            fos.close();

            //이벤트 전송
            sendEventAndFinish(pictureFile);

        } catch (FileNotFoundException e) {
            Log.d(this.getClass().getSimpleName(), "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(this.getClass().getSimpleName(), "Error accessing file: " + e.getMessage());
        }
    }


    public void sendEventAndFinish(final File image) {

        // Server로 전송해야됨
        final String deviceId = JmDnsUtils.ID;

        SharedPreferences sharedPreferences = getSharedPreferences("intercom", MODE_PRIVATE);
        final String hostAddress = sharedPreferences.getString("remote-addr", null);

        if (hostAddress != null) {

            final Event event = new Event();
            event.setDeviceID(deviceId);
            event.setType("guest");
            event.setTimestamp(new Date());
            event.setValue(1);

            new AsyncTask<Void, Void, Integer>() {
                @Override
                protected Integer doInBackground(Void... objects) {
                    System.out.println("GuestHistory : sending event to " + hostAddress);

                    int result = EventSender.sendEvent(hostAddress, event, image);
                    System.out.println("GuestHistory : event send result : " + result);

                    return result;
                }

                @Override
                protected void onPostExecute(Integer result) {
                    mProgressBar.setVisibility(View.GONE);
                    TextView text = (TextView) findViewById(R.id.text);
                    if (result == 200) {
                        Toast.makeText(getApplicationContext(), "전송되었습니다.", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(CameraActivity.this, MainActivity.class));
                        finish();
                    } else {
                        text.setText("전송에 실패하였습니다. ");
                        Toast.makeText(getApplicationContext(), "전송에 실패하였습니다. (" + result + ")", Toast.LENGTH_SHORT).show();
                        mCamera.startPreview();
                    }
                }
            }.execute(null, null, null);

        }
    }

    /**
     * Check if this device has a camera
     */
    private static boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    /**
     * A safe way to get an instance of the Camera object.
     */
    public Camera getCameraInstance() {
        Camera c = null;
        for (int id = 0; id < Camera.getNumberOfCameras(); id++) {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(id, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) try {
                c = Camera.open(id); // attempt to get a Camera instance
                Camera.Parameters p = c.getParameters();

                p.set("jpeg-quality", 80);

                p.set("orientation", "landscape");
                p.set("rotation", 270);
                p.setPictureFormat(ImageFormat.JPEG);

                c.setParameters(p);
                break;
            } catch (Exception e) {
                e.printStackTrace();
                // Camera is not available (in use or does not exist)
            }
        }
        return c; // returns null if camera is unavailable
    }


    /**
     * Release opened camera
     */
    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }

    /**
     * A basic Camera preview class
     */
    public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
        private SurfaceHolder mHolder;
        private Camera mCamera;

        public CameraPreview(Context context, Camera camera) {
            super(context);
            mCamera = camera;

            // Install a SurfaceHolder.Callback so we get notified when the
            // underlying surface is created and destroyed.
            mHolder = getHolder();
            mHolder.addCallback(this);
            // deprecated setting, but required on Android versions prior to 3.0
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        public void surfaceCreated(SurfaceHolder holder) {
            // The Surface has been created, now tell the camera where to draw the preview.
            if (mCamera != null) try {
                Camera.Parameters parameters = mCamera.getParameters();
                if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
                    mCamera.setDisplayOrientation(90);
                    //parameters.setRotation(90);
                } else {
                    // This is an undocumented although widely known feature
                    // parameters.set("orientation", "landscape");
                    // For Android 2.2 and above
                    mCamera.setDisplayOrientation(0);
                    // Uncomment for Android 2.0 and above
                    // parameters.setRotation(0);
                }

                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
            } catch (IOException e) {
                Log.d(this.getClass().getSimpleName(), "Error setting camera preview: " + e.getMessage());
            }
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            // empty. Take care of releasing the Camera preview in your activity.
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
            // If your preview can change or rotate, take care of those events here.
            // Make sure to stop the preview before resizing or reformatting it.

            if (mHolder.getSurface() == null) {
                // preview surface does not exist
                return;
            }

            // stop preview before making changes
            try {
                mCamera.stopPreview();
            } catch (Exception e) {
                // ignore: tried to stop a non-existent preview
            }

            // set preview size and make any resize, rotate or
            // reformatting changes here

            // start preview with new settings
            try {
                mCamera.setPreviewDisplay(mHolder);
                mCamera.setDisplayOrientation(90);
                mCamera.startPreview();

            } catch (Exception e) {
                Log.d(this.getClass().getSimpleName(), "Error starting camera preview: " + e.getMessage());
            }
        }
    }

    /**
     * Create a file Uri for saving an image or video
     */
    public static Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /**
     * Create a File for saving an image or video
     */
    public static File getOutputMediaFile(int type) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "AjouinoIntercom");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("AjouinoIntercom", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    public static Bitmap rotateImage(Bitmap src, float degree) {
        // create new matrix
        Matrix matrix = new Matrix();
        // setup rotation degree
        matrix.postRotate(degree);
        return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
    }

    public static Bitmap resizeImage(Bitmap src, int width, int height) {
        // create new matrix
        Matrix matrix = new Matrix();
        // setup scaled bitmap
        return Bitmap.createScaledBitmap(src, width, height, false);
    }
}



