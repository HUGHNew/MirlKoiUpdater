package com.hugh.updater.mirlkoi

import android.graphics.Bitmap
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ImageService {
    @GET("API/pc.php")
    fun pc(): Call<Bitmap>
    @GET("API/mp.php")
    fun mobile(): Call<Bitmap>
    @GET("api.php")
    fun getSort(@Query("type") type:String):Call<Bitmap>
}