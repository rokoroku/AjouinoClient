package kr.ac.ajouino.intercom;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.text.format.Formatter;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.Map;

import fi.iki.elonen.nanohttp.NanoHTTPD;
import kr.ac.ajouino.intercom.model.DeviceInfo;
import kr.ac.ajouino.intercom.util.JmDnsUtils;

public class HelloServerService extends Service {

    private final static String USERNAME = "root";
    private final static String PASSWORD = "arduino";
    private final static int PORT = 8080;

    public HelloServerService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return new Binder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("Ajouino Intercom")
                .setStyle(new NotificationCompat.BigTextStyle().bigText("Hello server is running..."))
                .setContentText("Hello server is running...");

        mBuilder.setContentIntent(contentIntent);
        startForeground(1, mBuilder.build());

        return super.onStartCommand(intent, flags, startId);
    }

    private static HelloServer server;


    /**
     * Called when the activity is first created.
     */

    @Override
    public void onCreate() {
        super.onCreate();
        if(server == null) try {
            server = new HelloServer();
            server.start();
            JmDnsUtils.register();

            Log.w("Httpd", "Web server initialized.");
        } catch (IOException ioe) {
            Log.w("Httpd", "The server could not start.");
            server = null;
        }
    }

    // DON'T FORGET to stop the server
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (server != null) {
            server.stop();
            //JmDnsUtils.unregister();
        }
    }

    private class HelloServer extends NanoHTTPD {

        public HelloServer() {
            super(PORT);
        }

        @Override
        public Response serve(String uri, Method method,
                              Map<String, String> header,
                              Map<String, String> parameters,
                              Map<String, String> files) {
            String answer = "";
            String[] uriParams = uri.split("/");

            String authHeader = header.get("authorization");
            if (authHeader != null && authHeader.equals(generateBasicAuthHeader(USERNAME, PASSWORD))) {
                if (uriParams.length > 1 && uriParams[1].equals("arduino")) {
                    if (uriParams.length > 2 && uriParams[2].equals("hello")) {
                        DeviceInfo deviceInfo = new DeviceInfo();
                        deviceInfo.setId(JmDnsUtils.ID);
                        deviceInfo.setLabel(JmDnsUtils.LABEL);
                        deviceInfo.setAddress(getHostAddress());
                        deviceInfo.setType("intercom");

                        Gson gson = new Gson();
                        answer = gson.toJson(deviceInfo);

                        String remoteAddress = new String(header.get("remote-addr").getBytes());
                        SharedPreferences sharedPreferences = getSharedPreferences("intercom", MODE_PRIVATE);
                        sharedPreferences.edit().putString("remote-addr", remoteAddress).apply();

                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    } else if (uriParams.length > 2 && uriParams[2].equals("bye")) {
                        String remoteAddress = new String(header.get("remote-addr").getBytes());
                        SharedPreferences sharedPreferences = getSharedPreferences("intercom", MODE_PRIVATE);

                        if (sharedPreferences.getString("remote-addr", "").equals(remoteAddress)) {
                            sharedPreferences.edit().clear().apply();
                            answer = "bye";

                            Intent intent = new Intent(getApplicationContext(), StandbyActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);

                        } else {
                            return new Response(Response.Status.UNAUTHORIZED, MIME_PLAINTEXT, "Unauthorized");
                        }
                    } else {
                        return new Response(Response.Status.BAD_REQUEST, MIME_PLAINTEXT, "Bad Request");
                    }
                }
                return new NanoHTTPD.Response(answer);
            } else {
                return new Response(Response.Status.UNAUTHORIZED, MIME_PLAINTEXT, "Unauthorized");
            }
        }
    }

    public String getHostAddress() {
        try {
            WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
            return Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String generateBasicAuthHeader(String username, String password) {
        String credential = username + ":" + password;
        String basicAuth = "Basic " + Base64.encodeToString(credential.getBytes(), Base64.NO_WRAP);
        return basicAuth;
    }
}
