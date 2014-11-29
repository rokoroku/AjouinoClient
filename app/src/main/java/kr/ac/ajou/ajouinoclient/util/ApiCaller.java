package kr.ac.ajou.ajouinoclient.util;

import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import kr.ac.ajou.ajouinoclient.model.Device;
import kr.ac.ajou.ajouinoclient.model.DeviceInfo;
import kr.ac.ajou.ajouinoclient.model.Event;

/**
 * Created by YoungRok on 2014-11-26.
 */
public class ApiCaller {

    private final static int HTTP_GET = 1;
    private final static int HTTP_POST = 2;
    private final static int HTTP_PUT = 3;
    private final static int HTTP_DELETE = 4;

    private static Gson gson = new Gson();
    private static ApiCaller instance;

    private final Queue<TaskRequestParam> requestQueue;

    private final String hostAddress;
    private String authToken;

    private APICallTask apiCallTask;

    public ApiCaller(String hostAddress) {
        if (!hostAddress.startsWith("http://")) hostAddress = "http://" + hostAddress;
        if (!hostAddress.endsWith("/")) hostAddress = hostAddress + "/";
        this.hostAddress = hostAddress;
        this.requestQueue = new ConcurrentLinkedQueue<>();
    }

    public static ApiCaller getStaticInstance() {
        return instance;
    }

    public static void setStaticInstance(ApiCaller instance) {
        ApiCaller.instance = instance;
    }

    public boolean postEvent(Event event) throws ApiException {
        StringBuilder sb = new StringBuilder()
                .append(hostAddress)
                .append("device/")
                .append(event.getDeviceID())
                .append("/event/");
        String respond = request(HTTP_POST, sb.toString(), gson.toJson(event));
        return true;
    }

    public void postEventAsync(Event event, Callback callback) {
        TaskRequestParam param = new TaskRequestParam(HTTP_POST, event, callback);
        callAPIAsync(param);
    }

    public Device postDevice(Device device) throws ApiException {
        StringBuilder sb = new StringBuilder()
                .append(hostAddress)
                .append("device/");
        String respond = request(HTTP_POST, sb.toString(), gson.toJson(device));
        return gson.fromJson(respond, Device.class);
    }

    public void postDeviceAsync(Device device, Callback callback) {
        TaskRequestParam param = new TaskRequestParam(HTTP_POST, device, callback);
        callAPIAsync(param);
    }

    public Device getDevice(String deviceId) throws ApiException {
        StringBuilder sb = new StringBuilder()
                .append(hostAddress)
                .append("device/")
                .append(deviceId);
        String respond = request(HTTP_GET, sb.toString(), null);
        return gson.fromJson(respond, Device.class);
    }

    public void getDeviceAsync(String deviceId, Callback callback) {
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.setType("registered");
        TaskRequestParam param = new TaskRequestParam(HTTP_GET, deviceInfo, callback);
        callAPIAsync(param);
    }

    public Collection<Device> getDevices() throws ApiException {
        StringBuilder sb = new StringBuilder()
                .append(hostAddress)
                .append("devices/");
        String respond = request(HTTP_GET, sb.toString(), null);
        Type listType = new TypeToken<List<Device>>() {
        }.getType();
        return gson.fromJson(respond, listType);
    }

    public void getDevicesAsync(Callback callback) {
        DeviceInfo deviceInfo = new DeviceInfo();
        TaskRequestParam param = new TaskRequestParam(HTTP_GET, deviceInfo, callback);
        callAPIAsync(param);
    }

    public Collection<DeviceInfo> getAllDevicesOnNetwork() throws ApiException {
        StringBuilder sb = new StringBuilder()
                .append(hostAddress)
                .append("devices/lookup");
        String respond = request(HTTP_GET, sb.toString(), null);

        Type listType = new TypeToken<List<DeviceInfo>>() {
        }.getType();
        return gson.fromJson(respond, listType);
    }

    public void getAllDevicesOnNetworkAsync(Callback callback) {
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.setType("lookup");
        TaskRequestParam param = new TaskRequestParam(HTTP_GET, deviceInfo, callback);
        callAPIAsync(param);
    }

    public Device removeDevice(String deviceId) throws ApiException {
        StringBuilder sb = new StringBuilder()
                .append(hostAddress)
                .append("device/")
                .append(deviceId);
        String respond = request(HTTP_DELETE, sb.toString(), null);
        return gson.fromJson(respond, Device.class);
    }

    public void removeDeviceAsync(String deviceId, Callback callback) {
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.setId(deviceId);
        TaskRequestParam param = new TaskRequestParam(HTTP_DELETE, deviceId, callback);
        callAPIAsync(param);
    }

    private HttpRequest generateHttpRequest(int method, String URL, String entity) {
        HttpRequest httpRequest;
        switch (method) {
            case HTTP_GET:
                httpRequest = new HttpGet(URL);
                break;
            case HTTP_POST:
                httpRequest = new HttpPost(URL);
                if (entity != null) try {
                    ((HttpPost) httpRequest).setEntity(new StringEntity(entity));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                break;
            case HTTP_PUT:
                httpRequest = new HttpPut(URL);
                if (entity != null) try {
                    ((HttpPut) httpRequest).setEntity(new StringEntity(entity));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                break;
            case HTTP_DELETE:
                httpRequest = new HttpDelete(URL);
                break;
            default:
                httpRequest = new HttpGet(URL);
        }
        return httpRequest;
    }

    private String request(int method, String URL, String entity) throws ApiException {
        try {
            HttpClient httpClient = AndroidHttpClient.newInstance("Ajouino/Android");
            HttpRequest httpRequest = generateHttpRequest(method, URL, entity);
            Log.d(this.getClass().getSimpleName(), "Requesting " + httpRequest.getRequestLine().getMethod() + " " + httpRequest.getRequestLine().getUri());

            int timeoutConnection = 10000;
            int timeoutSocket = 20000;
            HttpParams httpParameters = httpClient.getParams();
            HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
            HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);

            httpRequest.setHeader("Content-Type", "application/json");
            if (authToken != null) httpRequest.setHeader("Authorization", "Basic " + authToken);

            HttpResponse response = httpClient.execute((HttpUriRequest) httpRequest);

            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                return EntityUtils.toString(response.getEntity());
            } else {
                String statusMessage = response.getStatusLine().getReasonPhrase();
                throw new ApiException(statusCode, statusMessage);
            }

        } catch (IOException e) {
            e.printStackTrace();
            throw new ApiException(ApiException.ERROR_BAD_REQUEST, e.getMessage());
        }
    }

    public boolean authenticate(String authToken) throws ApiException {
        this.authToken = authToken;
        String response = request(HTTP_GET, hostAddress + "session/", null);
        return true;
    }

    private class TaskRequestParam {
        int method;
        Object param;
        Callback callback;

        private TaskRequestParam(int method, Object param, Callback callback) {
            this.method = method;
            this.param = param;
            this.callback = callback;
        }
    }

    private void callAPIAsync(TaskRequestParam param) {
        synchronized (requestQueue) {
            //add & excute request
            requestQueue.add(param);
            if (requestQueue.size() > 0 && apiCallTask == null) {
                apiCallTask = new APICallTask(requestQueue.peek());
                apiCallTask.execute();
            }
        }
    }

    public class APICallTask extends AsyncTask<Void, Void, Object> {

        private TaskRequestParam requestParam = null;
        Integer errorCode = null;

        public APICallTask(TaskRequestParam requestParam) {
            this.requestParam = requestParam;
        }

        @Override
        protected Object doInBackground(Void... input) {
            try {
                switch (requestParam.method) {
                    case HTTP_GET:
                        if (requestParam.param instanceof DeviceInfo) {
                            DeviceInfo deviceInfo = (DeviceInfo) requestParam.param;
                            if (deviceInfo.getId() != null) {
                                return getDevice(deviceInfo.getId());
                            } else if (deviceInfo.getType() == null) {
                                    return getDevices();
                            } else if (deviceInfo.getType().equals("lookup")) {
                                return getAllDevicesOnNetwork();
                            }
                        }
                        break;
                    case HTTP_POST:
                        if (requestParam.param instanceof Event) {
                            Event event = (Event) requestParam.param;
                            return postEvent(event);
                        } else if (requestParam.param instanceof Device) {
                            Device device = (Device) requestParam.param;
                            return postDevice(device);
                        }

                        break;
                    case HTTP_PUT:
                        break;
                    case HTTP_DELETE:
                        if (requestParam.param instanceof Device) {
                            Device device = (Device) requestParam.param;
                            removeDevice(device.getId());
                            return true;
                        }
                        break;
                    default:
                        return null;
                }

            } catch (Exception e) {
                Log.e(this.getClass().getSimpleName(), e.getMessage() + ", method: " + requestParam.method + ", param: " + requestParam.param);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Object result) {
            apiCallTask = null;

            synchronized (requestQueue) {
                //execute callbacks
                requestQueue.poll();
                if (requestParam.callback != null) {
                    // call success callback if result is not null
                    if (result != null) requestParam.callback.onSuccess(result);
                    else requestParam.callback.onFailure();
                }

                // excute next request
                if (requestQueue.size() > 0 && apiCallTask == null) {
                    apiCallTask = new APICallTask(requestQueue.peek());
                    apiCallTask.execute();
                }
            }

        }
    }
}