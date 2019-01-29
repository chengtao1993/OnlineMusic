package com.archermind.music.utils;

import android.app.Activity;
import android.content.Context;

public class Utils {

    private static Context sContext;

    public static void init(Context context) {
        sContext = context.getApplicationContext();
    }

}
