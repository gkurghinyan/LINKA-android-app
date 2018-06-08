package com.linka.lockapp.aos.module.api;


import com.pixplicity.easyprefs.library.Prefs;

import java.io.IOException;
import java.security.cert.CertificateException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Vanson on 10/6/2016.
 */
public class LinkaAPIServiceManager {

    public void saveAuth(String authToken, String userId) {
        Prefs.edit().putString("x-auth-token", authToken).commit();
        Prefs.edit().putString("x-user-id", userId).commit();
    }

    public void clearAuth() {
        Prefs.edit().remove("x-auth-token").commit();
        Prefs.edit().remove("x-user-id").commit();
    }

    static LinkaAPIService service;
    static Retrofit retrofit;
    static OkHttpClient client;

    public static Retrofit getRetrofit() {
        return retrofit;
    }

    public static OkHttpClient getClient(){
        return client;
    }

    public static LinkaAPIServiceResponse extractErrorFromResponse(retrofit2.Response response) {
        if (response.errorBody() == null) {
            return null;
        }
        try {
            Converter<okhttp3.ResponseBody, LinkaAPIServiceResponse> converter = getRetrofit().responseBodyConverter(LinkaAPIServiceResponse.class, LinkaAPIServiceResponse.class.getAnnotations());
            LinkaAPIServiceResponse error = converter.convert(response.errorBody());
            return error;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static LinkaAPIService getInstance() {

        if (service != null) {
            return service;
        }

        SSLContext sslContext = null;
        SSLSocketFactory sslSocketFactory = null;

        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[] {
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            // Install the all-trusting trust manager
            sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            sslSocketFactory = sslContext.getSocketFactory();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

        if (sslSocketFactory != null)
        {
            httpClient.sslSocketFactory(sslSocketFactory);
            httpClient.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return hostname.contains("linkalock.com");
                }
            });
        }

        httpClient.addInterceptor(new Interceptor() {
                                      @Override
                                      public Response intercept(Interceptor.Chain chain) throws IOException {
                                          String authToken = Prefs.getString("x-auth-token", "");
                                          String userId = Prefs.getString("x-user-id", "");

                                          Request original = chain.request();

                                          Request request = original.newBuilder()
                                                  .header("x-auth-token", authToken)
                                                  .header("x-user-id", userId)
                                                  .method(original.method(), original.body())
                                                  .build();

                                          return chain.proceed(request);
                                      }
                                  });

        client = httpClient.build();
        retrofit = new Retrofit.Builder()
                .baseUrl(LinkaAPIServiceConfig.URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        service = retrofit.create(LinkaAPIService.class);
        return service;
    }
}
