package kr.ac.ajou.ajouinoclient.persistent;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.Map;

/**
 * Created by YoungRok on 2014-11-26.
 */
public class PreferenceManager extends Application {
    public final static String PREFERENCE_HOST_ADDRESS = "hostaddr";
    public final static String PREFERENCE_USERNAME = "username";
    public final static String PREFERENCE_PASSWORD = "password";

    private static PreferenceManager mInstance;

    private SharedPreferences preferences;
    private Map<String, ?> preferenceTable;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        preferences = mInstance.getSharedPreferences("preference", Context.MODE_PRIVATE);
        preferenceTable = preferences.getAll();
    }

    public PreferenceManager() {
    }

//    public static PreferenceManager getInstance(Context context) {
//        return mInstance;
//    }

    public static PreferenceManager getInstance() {
        return mInstance;
    }

    public String getString(String key) {
        return (String) preferenceTable.get(key);
    }

    public Integer getInt(String key) {
        return (Integer) preferenceTable.get(key);
    }

    public void put(String key, String value) {
        preferences.edit().putString(key, value).apply();
        preferenceTable = preferences.getAll();
    }

    public void put(String key, Integer value) {
        preferences.edit().putInt(key, value).apply();
        preferenceTable = preferences.getAll();
    }

    public void remove(String key) {
        preferences.edit().remove(key).apply();
        preferenceTable.remove(key);
    }

}
