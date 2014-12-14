package kr.ac.ajou.ajouinoclient.util;

import android.util.Base64;

public class AuthUtils {
    /**
     * Generate HTTP Basic Authorization Header
     * 
     * @param username username
     * @param password password
     * @return Generated header
     */
    public static String getPasswordDigest(String username, String password) {
        String authString = username + ":" + password;
        return Base64.encodeToString(authString.getBytes(), Base64.NO_WRAP);
    }
}
