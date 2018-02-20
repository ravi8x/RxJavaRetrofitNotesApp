package info.androidhive.rxjavaretrofit.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import info.androidhive.rxjavaretrofit.app.Const;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by ravi on 20/02/18.
 */

public class PrefUtils {
    private SharedPreferences pref;

    public PrefUtils() {
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences("APP_PREF", Context.MODE_PRIVATE);
    }

    public static void storeApiKey(Context context, String apiKey) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString("API_KEY", apiKey);
        editor.commit();
    }

    public static String getApiKey(Context context) {
        return getSharedPreferences(context).getString("API_KEY", null);
    }
}
