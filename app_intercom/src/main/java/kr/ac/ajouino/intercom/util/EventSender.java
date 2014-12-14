package kr.ac.ajouino.intercom.util;

import android.net.http.AndroidHttpClient;

import com.google.gson.Gson;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.FileEntity;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;

import kr.ac.ajouino.intercom.model.Event;


public class EventSender {

    public static int sendEvent(String URL, Event event, File image) {
        AndroidHttpClient androidHttpClient = null;

        try {
            Gson gson = new Gson();

            androidHttpClient = AndroidHttpClient.newInstance("Ajouino/Android");
            if(!URL.startsWith("http://")) URL = "http://" + URL;
            if(!URL.endsWith("/")) URL += "/";
            URL += "event/";

            HttpUriRequest httpRequest = new HttpPut(URL);

            int timeoutConnection = 10000;
            int timeoutSocket = 20000;
            HttpParams httpParameters = androidHttpClient.getParams();
            HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
            HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);

            httpRequest.setHeader("event", gson.toJson(event));

            FileEntity entity = new FileEntity(image, "image/jpeg");
            ((HttpPut)httpRequest).setEntity(entity);

            //httpRequest.setHeader("Authorization", "Basic " + authToken);
            HttpResponse response = androidHttpClient.execute(httpRequest);

            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                String result = EntityUtils.toString(response.getEntity());
                androidHttpClient.close();
            } else {
                String statusMessage = response.getStatusLine().getReasonPhrase();
                androidHttpClient.close();
            }
            return statusCode;

        } catch (IOException e) {
            e.printStackTrace();
            androidHttpClient.close();
            return -1;
        }
    }
}
