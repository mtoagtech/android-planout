package com.planout.retrofit

import com.planout.retromodel.HomeVisitorMainModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.http.*


interface ApiInterface {
    // For POST request
    /*@FormUrlEncoded // annotation that used with POST type request
    @POST("api/v1/stores") // specify the sub url for our base url
    fun login(
        @Field("user_email") user_email: String?,
        @Field("user_pass") user_pass: String?, callback: Callback<SignUpResponse?>?
    )*/

    //user_email and user_pass are the post parameters and SignUpResponse is a POJO class which recieves the response of this API
    // for GET request
    @GET("api/v1/stores") // specify the sub url for our base url
    fun getHomeVisitorList(): Call<HomeVisitorMainModel>?
}