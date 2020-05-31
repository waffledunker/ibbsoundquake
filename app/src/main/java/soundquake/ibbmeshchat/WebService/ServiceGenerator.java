package soundquake.ibbmeshchat.WebService;

import java.util.concurrent.TimeUnit;

import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Deniz on 30.05.2020.
 */
public class ServiceGenerator
{
    public static String API_URL = "http://34.107.62.224:8080/";
    
    public static Dispatcher dispatcher = new Dispatcher();
    public static OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder().dispatcher(dispatcher);
    public static OkHttpClient okhttpClient;
    
    private static Retrofit.Builder builder =
            new Retrofit.Builder()
                    .baseUrl(API_URL)
                    .addConverterFactory(GsonConverterFactory.create());
    
    public static <S> S createService(Class<S> serviceClass)
    {
        okhttpClient = httpClientBuilder
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS).build();
        Retrofit retrofit = builder.client(okhttpClient).build();
        return retrofit.create(serviceClass);
    }
    
    
}
