package info.androidhive.rxjavaretrofit.network;

import android.content.Context;
import android.text.TextUtils;

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import info.androidhive.rxjavaretrofit.app.Const;
import info.androidhive.rxjavaretrofit.utils.PrefUtils;
import okhttp3.CipherSuite;
import okhttp3.ConnectionSpec;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.TlsVersion;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by ravi on 20/02/18.
 */

public class ApiClient {
    private static Retrofit retrofit = null;
    private static int REQUEST_TIMEOUT = 60;
    private static OkHttpClient okHttpClient;

    public static Retrofit getClient(Context context) {

        if (okHttpClient == null)
            initOkHttp(context);

        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(Const.BASE_URL)
                    .client(okHttpClient)
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    private static void initOkHttp(final Context context) {
        OkHttpClient.Builder httpClient = new OkHttpClient().newBuilder()
                .connectTimeout(REQUEST_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(REQUEST_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(REQUEST_TIMEOUT, TimeUnit.SECONDS);

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        httpClient.addInterceptor(interceptor);

        httpClient.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request original = chain.request();
                Request.Builder requestBuilder = original.newBuilder()
                        .addHeader("Accept", "application/json")
                        .addHeader("Content-Type", "application/json");

                // Adding Authorization token (API Key)
                // Requests will be denied without API key
                if (!TextUtils.isEmpty(PrefUtils.getApiKey(context))) {
                    requestBuilder.addHeader("Authorization", PrefUtils.getApiKey(context));
                }

                Request request = requestBuilder.build();
                return chain.proceed(request);
            }
        });

        okHttpClient = httpClient.build();
    }
}
