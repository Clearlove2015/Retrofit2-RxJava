package com.zc.retrofit2rxjava;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.stetho.Stetho;
import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.google.gson.Gson;

import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * 干货集中营 API 文档：http://gank.io/api
 *
 *  Android Retrofit2&OkHttp3添加统一的请求头Header：
 * http://blog.csdn.net/jdsjlzx/article/details/51578231
 *
 * Android Retrofit2.0 查看log和JSON字符串（HttpLoggingInterceptor）：
 * http://blog.csdn.net/jdsjlzx/article/details/51520945
 *
 * Stetho用法：运行App, 打开Chrome输入chrome://inspect/#devices
 * http://www.jianshu.com/p/03da9f91f41f
 */
public class MainActivity extends AppCompatActivity {

    @Bind(R.id.btn_request)
    Button btnRequest;
    @Bind(R.id.tv_content)
    TextView tvContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Stetho.initializeWithDefaults(this);//初始化Stetho网络请求调试
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.btn_request)
    public void onViewClicked() {
        retrofit_Request();
    }

    private void retrofit_Request() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new HeaderInterceptor())//添加请求头
                .addInterceptor(logging)//打印日志
                .addNetworkInterceptor(new StethoInterceptor())//添加Stetho网络请求调试拦截
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(UriMethod.GANKIO_API)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();

        ApiClient apiClient = retrofit.create(ApiClient.class);

        /**
         * Rxtrofit与RxJava配合使用
         * 被观察者（Observable）-->订阅（subscribe）-->观察者（Observer & Subscriber）
         */
        apiClient.getSearchAPI()
                .subscribeOn(Schedulers.io())//io线程
                .observeOn(AndroidSchedulers.mainThread())//ui线程
                .subscribe(new Subscriber<ResultBean>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(ResultBean resultBean) {
                        Gson gson = new Gson();
                        String json = gson.toJson(resultBean);
                        tvContent.setText(json);
                    }
                });
    }

    /**
     * 添加请求头信息
     */
    class HeaderInterceptor implements Interceptor {

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request()
                    .newBuilder()
                    .addHeader("Access-Control-Allow-Origin", "*")
                    .addHeader("cache-control", "no-cache")
                    .addHeader("Content-Security-Policy", "script-src 'self'; object-src 'self'")
                    .addHeader("Content-Type", "text/css")
                    .addHeader("ETag", "GENtWV50c/kk3N8BxyGHc/MwfHA=")
                    .build();
            return chain.proceed(request);
        }
    }

}
