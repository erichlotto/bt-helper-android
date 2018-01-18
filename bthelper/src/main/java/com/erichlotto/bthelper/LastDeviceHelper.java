package com.erichlotto.bthelper;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by erich on 1/18/18.
 */

public class LastDeviceHelper {

    private static final String SHARED_PREFERENCES_NAME = "SHARED_PREFERENCES";
    private static final String KEY_ADDRESS = "ADDRESS";

    public static void storeLastAddress(Context context, String address) {
        SharedPreferences.Editor editor = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).edit();
        editor.putString(KEY_ADDRESS, address);
        editor.apply();
    }

    public static String getLastAddress(Context context){
        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_ADDRESS, null);
    }


}
