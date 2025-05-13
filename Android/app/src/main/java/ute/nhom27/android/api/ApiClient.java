package ute.nhom27.android.api;

import android.content.Context;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import ute.nhom27.android.utils.SharedPrefManager;

public class ApiClient {
    //private static final String BASE_URL = "http://192.168.0.103:8081/";
    private static final String BASE_URL = "http://10.0.2.2:8081/";
    private static Retrofit noAuthRetrofit = null;
    private static Retrofit authRetrofit = null;

    // Dùng khi chưa login
    public static Retrofit getNoAuthClient() {
        if (noAuthRetrofit == null) {
            noAuthRetrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return noAuthRetrofit;
    }

    public static Retrofit getAuthClient(Context context) {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    String token = new SharedPrefManager(context).getToken();
                    Request request = chain.request().newBuilder()
                            .addHeader("Authorization", "Bearer " + token)
                            .build();
                    return chain.proceed(request);
                }).build();

        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }
}
