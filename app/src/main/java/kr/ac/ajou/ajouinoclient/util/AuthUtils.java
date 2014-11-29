package kr.ac.ajou.ajouinoclient.util;

import android.util.Base64;

/**
 * Created by YoungRok on 2014-11-26.
 */
public class AuthUtils {
    public static String getPasswordDigest(String username, String password) {
        String authString = username + ":" + password;
        return Base64.encodeToString(authString.getBytes(), Base64.NO_WRAP);
    }
}
