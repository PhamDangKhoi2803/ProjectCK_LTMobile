package ute.nhom27.android.api;

import android.content.Context;
import android.util.Log;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import ute.nhom27.android.utils.SharedPrefManager;

public class ApiClient {
    //private static final String BASE_URL = "http://192.168.0.103:8081/";
    private static final String BASE_URL = "http://172.16.31.65:8081/";
    private static final String OPEN_AI_BASE_URL = "https://api.cohere.ai/";
    private static Retrofit noAuthRetrofit = null;
    private static Retrofit authRetrofit = null;
    private static Retrofit openAiRetrofit = null;

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

    //    public static Retrofit getAuthClient(Context context) {
//        OkHttpClient client = new OkHttpClient.Builder()
//                .addInterceptor(chain -> {
//                    String token = new SharedPrefManager(context).getToken();
//                    Request request = chain.request().newBuilder()
//                            .addHeader("Authorization", "Bearer " + token)
//                            .build();
//                    return chain.proceed(request);
//                }).build();
//
//        return new Retrofit.Builder()
//                .baseUrl(BASE_URL)
//                .client(client)
//                .addConverterFactory(GsonConverterFactory.create())
//                .build();
//    }
    public static Retrofit getAuthClient(Context context) {
        if (authRetrofit == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        String token = new SharedPrefManager(context).getToken();
                        Log.d("ApiClient", "Token: " + token);

                        Request original = chain.request();
                        Log.d("ApiClient", "Request URL: " + original.url());
                        Log.d("ApiClient", "Request method: " + original.method());

                        Request request = original.newBuilder()
                                .addHeader("Authorization", "Bearer " + token)
                                .build();

                        Log.d("ApiClient", "Request headers: " + request.headers());

                        Response response = chain.proceed(request);
                        Log.d("ApiClient", "Response code: " + response.code());
                        Log.d("ApiClient", "Response headers: " + response.headers());

                        return response;
                    })
                    .build();

            authRetrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return authRetrofit;
    }

    public static Retrofit getOpenAiClient(String apiKey) {
        if (openAiRetrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        Request original = chain.request();
                        Request request = original.newBuilder()
                                .addHeader("Authorization", "Bearer " + apiKey)
                                .addHeader("Content-Type", "application/json")
                                .build();
                        return chain.proceed(request);
                    })
                    .addInterceptor(logging)
                    .build();

            openAiRetrofit = new Retrofit.Builder()
                    .baseUrl(OPEN_AI_BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return openAiRetrofit;
    }
}
