package kr.ac.ajouino.intercom;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import kr.ac.ajouino.intercom.model.Event;
import kr.ac.ajouino.intercom.util.JmDnsUtils;
import kr.ac.ajouino.intercom.util.EventSender;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;

public class HouseholderCallActivity extends Activity implements Camera.PictureCallback {

    // Define
    TextView text;
    TextView time;
    Button cancel;
    CountDownTimer timer;
    int failcode = 10;
    int value = 0;
    int mnMillisecond = 1000;
    int mnexitdelay = 60;
    Camera camera = null;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_connection);

        text = (TextView) findViewById(R.id.text);
        time = (TextView) findViewById(R.id.time);
        cancel = (Button) findViewById(R.id.cancel);
        value = 0;

        int delay = mnexitdelay * mnMillisecond;

        // Call Thread 호출
        // callThread ct = new callThread();
        // ct.start();

        // Timer 30 초 설정
        timer = new CountDownTimer(delay, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                // TODO Auto-generated method stub
                value++;
                time.setText("0:" + Integer.toString(value));
                if(value == 1) {
                    camera.takePicture(null, null, HouseholderCallActivity.this);
                }
            }

            @Override
            public void onFinish() {
                // TODO Auto-generated method stub
                Log.v("TIMER", "1분 지났습니다.");
                finish();
            }
        };

        timer.start();

        // 취소버튼 누를 경우
        cancel.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                timer.cancel();
                value = 0;
                Log.v("CONNECTION", "취소버튼 눌림");
                finish();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (camera == null) {
            camera = getCameraInstance();
        }
    }

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

                //p.setPictureSize(600,800);
                p.setPictureFormat(ImageFormat.JPEG);
                //p.setPreviewSize(h, w);// here w h are reversed
                c.setParameters(p);
                break;
            } catch (Exception e) {
                e.printStackTrace();
                // Camera is not available (in use or does not exist)
            }
        }
        return c; // returns null if camera is unavailable
    }


    @Override
    public void onPictureTaken(byte[] data, Camera camera) {

        File pictureFile = CameraActivity.getOutputMediaFile(CameraActivity.MEDIA_TYPE_IMAGE);
        if (pictureFile == null) {
            Log.d(this.getClass().getSimpleName(), "Error creating media file, check storage permissions");
            return;
        }

        try {
            //이미지 저장
            if (Build.MANUFACTURER.contains("samsung")) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                bitmap = CameraActivity.rotateImage(bitmap, 270);

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
                data = stream.toByteArray();
            }

            FileOutputStream fos = new FileOutputStream(pictureFile);
            fos.write(data);
            fos.close();

            //이벤트 전송
            sendEvent(pictureFile);

        } catch (FileNotFoundException e) {
            Log.d(this.getClass().getSimpleName(), "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(this.getClass().getSimpleName(), "Error accessing file: " + e.getMessage());
        }
    }


    public void sendEvent(final File image) {

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
                    if (result == 200) {
                        Toast.makeText(getApplicationContext(), "이벤트가 전송되었습니다.", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(HouseholderCallActivity.this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), "이벤트 전송에 실패하였습니다. (" + result + ")", Toast.LENGTH_SHORT).show();
                    }
                }
            }.execute(null, null, null);

        }
    }

    // Call Thread : Server로 호출 명령을 보냄
    // 사용되지 않음
    public class callThread extends Thread {
        int frequency = 22050;
        //	    	int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
        //	    	int EncodingBitRate = AudioFormat.ENCODING_PCM_16BIT;
        //	    	AudioRecord audioRecord = null;
        int recBufSize = 0;
        boolean isRecording = false;
        Socket socket;
        ServerSocket receiveSocket;
        Socket clientsocket;
        String serverIP = "192.168.0.47";
        int send_port = 9000;
        int receive_port = 9010;
        DataOutputStream dos;
        DataInputStream dis;
        byte[] bytes = new byte[1024];

        public void run() {
            try {
                // 소켓연결 후 서버에 호출 시그널 전송
                socket = new Socket(serverIP, send_port);
                String call = "Call";
                byte[] databuffer = new byte[1024];

                databuffer = call.getBytes();
                dos = new DataOutputStream(socket.getOutputStream());

                dos.write(databuffer);
                dos.flush();
                Log.d("CALL THREAD", "THREAD: " + call);

                // 서버로부터의 응답을 기다림
                dos.close();
                socket.close();

                receiveSocket = new ServerSocket(receive_port);
                clientsocket = receiveSocket.accept();

                Log.v("CONNECTION", "CONNECTION");
                // 이부분 미구현 ㅁㅇㄻㅇㄻㅇㄻㅇㄴㄻㅇㄻㅇㅇㄹ
                // ㅁㅇㄻㅇㄻㅇㄻㅇㄻㅇㄻㅇㄹ
                byte[] receivebuffer = new byte[1024];
                dis = new DataInputStream(clientsocket.getInputStream());

                dis.read(receivebuffer);

                String readData = new String(receivebuffer, 0, receivebuffer.length);

                readData.trim();

                if (readData.equals("Ok")) {

                    //	adfadf

                } else if (readData.equals("No")) {

                    finish();
                }


            } catch (UnknownHostException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
    }
}


