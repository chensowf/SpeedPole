package org.speedpole.cache;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Admin on 2018/5/22.
 */

public class Preferences {

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private static class Singleton
    {
        public static Preferences instance;

        public static Preferences getInstance(Context context)
        {
            if(instance == null)
                instance = new Preferences(context);
            return instance;
        }

    }

    private Preferences(Context context){
        sharedPreferences = context.getSharedPreferences("userInfo", Activity.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public static Preferences getInstance(Context context)
    {
        return Singleton.getInstance(context);
    }



}
