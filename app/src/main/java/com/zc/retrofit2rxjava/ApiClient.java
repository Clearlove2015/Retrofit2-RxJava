package com.zc.retrofit2rxjava;

import retrofit2.http.GET;
import rx.Observable;

/**
 * Created by zc on 2017/12/16.
 */

public interface ApiClient {

    @GET(UriMethod.GANKIO_SEARCH)
    Observable<ResultBean> getSearchAPI();

}
