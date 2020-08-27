package com.example.messengerapp.Fragments;

import com.example.messengerapp.Notifications.MyResponse;
import com.example.messengerapp.Notifications.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ApiService
{
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAATVmTkNE:APA91bEBa0z0ItBEeSwN7n1YoBySVbOkvAd19Sxsa_mYHcFmXMb3Wn_8jlUfw9gYBnN5tjb_xIzqjBtM0jrKH-41c430BUrNTMc93_WBy7V7w8nlzyld7QFTC2XMAq1f6bloTAIPZCGS"
    })

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
