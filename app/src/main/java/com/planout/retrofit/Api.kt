package com.planout.retrofit

import com.planout.retrofit.ApiInterface
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object Api {
    //Setting the Root URL
    //Finally building the adapter

    //Creating object for our interface
    val client: ApiInterface
        get() {
            val adapter = Retrofit.Builder()
                .baseUrl("https://www.planoutapp.co/") //Setting the Root URL
                .addConverterFactory(GsonConverterFactory.create())
                .build() //Finally building the adapter

            //Creating object for our interface
            return adapter.create(ApiInterface::class.java)
        }
}