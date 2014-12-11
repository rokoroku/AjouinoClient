package kr.ac.ajou.ajouinoclient.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;

import java.util.Collection;
import java.util.HashSet;

import kr.ac.ajou.ajouinoclient.activity.DeviceActivity;
import kr.ac.ajou.ajouinoclient.R;
import kr.ac.ajou.ajouinoclient.model.Device;
import kr.ac.ajou.ajouinoclient.model.Event;
import kr.ac.ajou.ajouinoclient.persistent.DeviceManager;
import kr.ac.ajou.ajouinoclient.util.ApiCaller;
import kr.ac.ajou.ajouinoclient.util.Callback;
import kr.ac.ajou.ajouinoclient.util.ImageDownloader;

public class GcmIntentService extends IntentService {
    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    private ImageDownloader mImageDownloader;

    public GcmIntentService() {
        super("GcmIntentService");
    }


    public interface onNewGcmMessageListener {
        public void onNewEvent(Event event);
    }

    public static Collection<onNewGcmMessageListener> mListeners = new HashSet<>();

    public static void addOnNewGcmMessageListener(onNewGcmMessageListener listener) {
        mListeners.add(listener);
    }

    public static void removeOnNewGcmMessageListener(onNewGcmMessageListener listener) {
        mListeners.remove(listener);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM
             * will be extended in the future with new message types, just ignore
             * any message types you're not interested in, or that you don't
             * recognize.
             */
            if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                // Post notification of received message.
                Gson gson = new Gson();
                Event event = gson.fromJson(extras.getString("msg"), Event.class);

                sendNotification(event);
                Log.i(this.getClass().getSimpleName(), "Received: " + extras.toString());

                if (event != null && event.getDeviceID() != null) {
                    Device device = DeviceManager.getInstance().getDevice(event.getDeviceID());
                    if (device != null) device.addEvent(event);

                    // Invoke listener
                    for(onNewGcmMessageListener listener : mListeners) {
                        listener.onNewEvent(event);
                    }

                }
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                //sendNotification("Send error: " + extras.toString());
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                //sendNotification("Deleted messages on server: " + extras.toString());
            }


        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(Event event) {
        mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent deviceActivityIntent = new Intent(this, DeviceActivity.class);
        deviceActivityIntent.putExtra(DeviceActivity.PARAM_DEVICE_ID, event.getDeviceID());
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, deviceActivityIntent, 0);

        String msg = "New guest just visited to your home!";

        String label = event.getDeviceID();
        if(event.getDeviceID() != null) {
            Device device = DeviceManager.getInstance().getDevice(event.getDeviceID());
            if(device != null) label = device.getLabel();
        }

        if(mImageDownloader == null) mImageDownloader = new ImageDownloader();
        String imageResUrl = ApiCaller.getStaticInstance().getHostAddress() + "event/image/" + event.getDeviceID() + "_" + event.getTimestamp().getTime();

        final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setContentTitle("New Guest")
                .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                .setTicker("New guest arrived!")
                .setContentText(msg)
                .setContentInfo(label)
                .setSmallIcon(R.drawable.ic_launcher)
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE)
                .setSound(Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.raw.ringtone))
                .setContentIntent(contentIntent);

        mImageDownloader.downloadBitmapAsync(imageResUrl, new Callback() {
            @Override
            public void onSuccess(Object result) {
                if(result != null && result instanceof Bitmap) {
                    Bitmap bitmap = (Bitmap) result;

                    float multiplier= ImageDownloader.getImageFactor(getResources());
                    int bitmapSize = (int) (192*multiplier);

                    mBuilder.setLargeIcon(Bitmap.createScaledBitmap(bitmap, bitmapSize, bitmapSize, false));
                    mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
                } else {
                    this.onFailure();
                }
            }

            @Override
            public void onFailure() {
                mBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher));
                mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
            }
        });
    }
}