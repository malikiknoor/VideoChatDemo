package com.iknoortech.videochatdemo.notification;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

import static com.iknoortech.videochatdemo.helper.AppConstant.serverKey;


public interface ApiInterface {

    @Headers({
            "Content-Type:application/json",
            "Authorization:key="+serverKey
    })

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);

}
