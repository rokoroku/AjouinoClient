package kr.ac.ajouino.intercom.util;

import android.os.AsyncTask;

import java.io.IOException;
import java.util.HashMap;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceInfo;

/**
 * Sample Code for Service Registration using JmDNS.
 *
 */
public class JmDnsUtils {

    public final static String REMOTE_TYPE = "_ajouino._tcp.local.";
    public final static String ID = "AjouinoIntercom";
    public final static String LABEL = "Ajouino Intercom";

    private static JmDNS jmdns = null;
    private static String hostAddress = null;
    private static ServiceInfo pairService = null;

    public static void register() {
        if(pairService != null) return;
        new AsyncTask<Object, Object, Object>() {

            @Override
            protected Object doInBackground(Object[] objects) {
                try {
                    System.out.println("Opening JmDNS...");
                    if(jmdns == null) jmdns = JmDNS.create();

                    System.out.println("Opened JmDNS!");
                    int id = 10000;

                    final HashMap<String, String> values = new HashMap<String, String>();
                    values.put("id", "Android-" + id);
                    values.put("label", LABEL);

                    System.out.println("Requesting pairing for " + ID);
                    pairService = ServiceInfo.create(REMOTE_TYPE, ID, 8080, 0, 0, values);
                    jmdns.registerService(pairService);

                    System.out.println("\nRegistered Service as " + pairService);

//            jmdns.unregisterService(pairservice);
//            jmdns.unregisterAllServices();
//            jmdns.close();

                } catch (IOException e) {
                    e.printStackTrace();
                    pairService = null;
                }
                return null;
            }
        }.execute(null, null, null);
    }

    public static void unregister() {
        if(pairService == null) return;
        new AsyncTask<Object, Object, Object>() {

            @Override
            protected Object doInBackground(Object[] objects) {
                try {
                    if(jmdns == null) jmdns = JmDNS.create();
                    jmdns.unregisterAllServices();

                    System.out.println("Unregister all services.");

                } catch (IOException e) {
                    e.printStackTrace();
                }
                pairService = null;
                return null;
            }
        }.execute(null, null, null);
    }

}
