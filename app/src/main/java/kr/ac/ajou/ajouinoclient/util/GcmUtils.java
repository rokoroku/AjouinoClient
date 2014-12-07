package kr.ac.ajou.ajouinoclient.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

import kr.ac.ajou.ajouinoclient.model.User;
import kr.ac.ajou.ajouinoclient.persistent.PreferenceManager;

/**
 * Created by YoungRok on 2014-12-05.
 */
public class GcmUtils {

    private final static String SENDER_ID = "397044167566";

    private static GoogleCloudMessaging gcm;
    private static String regid = null;


    /**
     * Register current device to the GCM server asynchronously
     *
     * @param context application's context.
     */
    public static void register(final Context context) {
        new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;

                    // You should send the registration ID to your server over HTTP,
                    // so it can use GCM/HTTP or CCS to send messages to your app.
                    // The request to your server should be authenticated if your app
                    // is using accounts.
                    sendRegistrationIdToBackend();

                    // For this demo: we don't need to send it because the device
                    // will send upstream messages to a server that echo back the
                    // message using the 'from' address in the message.

                    // Persist the regID - no need to register again.
                    storeRegistrationId(context, regid);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }


            @Override
            protected void onPostExecute(Object o) {
                Log.d(context.getClass().getSimpleName(), o.toString());
            }
        }.execute(null, null, null);
    }

    /**
     * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP
     * or CCS to send messages to your app.
     */
    private static void sendRegistrationIdToBackend() {
        if (regid != null) {
            User user = new User();
            PreferenceManager preferenceManager = PreferenceManager.getInstance();
            String username = preferenceManager.getString(PreferenceManager.PREFERENCE_USERNAME);
            user.setId(username);
            user.setGcmId(regid);

            if(ApiCaller.getStaticInstance() != null) {
                ApiCaller.getStaticInstance().postUserAsync(user, new Callback() {
                    @Override
                    public void onSuccess(Object result) {
                        Log.d(this.getClass().getSimpleName(), "GCM key Successfully registered to the server");
                    }

                    @Override
                    public void onFailure() {
                        Log.d(this.getClass().getSimpleName(), "Failed to register GCM key to the server");
                    }
                });
            }
        }
    }

    /**
     * Stores the registration ID and app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId   registration ID
     */
    private static void storeRegistrationId(Context context, String regId) {
        SharedPreferences prefs = context.getSharedPreferences("gcm", Context.MODE_PRIVATE);
        prefs.edit().putString("regid", regId).apply();
    }
}
