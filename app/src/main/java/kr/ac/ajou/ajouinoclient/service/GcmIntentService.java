package kr.ac.ajou.ajouinoclient.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;

import kr.ac.ajou.ajouinoclient.DeviceActivity;
import kr.ac.ajou.ajouinoclient.R;
import kr.ac.ajou.ajouinoclient.model.Device;
import kr.ac.ajou.ajouinoclient.model.Event;
import kr.ac.ajou.ajouinoclient.persistent.DeviceManager;

public class GcmIntentService extends IntentService {
    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;

    public GcmIntentService() {
        super("GcmIntentService");
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

        String msg = "Guest visited at " + event.getTimestamp().toString();

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("New Guest")
                .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                .setContentInfo(event.getDeviceID())
                .setTicker("Guest visited!")
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE)
                .setContentText(msg);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}