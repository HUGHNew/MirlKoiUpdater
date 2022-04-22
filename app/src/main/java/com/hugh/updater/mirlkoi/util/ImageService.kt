package com.hugh.updater.mirlkoi.util

import android.graphics.Bitmap
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ImageService {
    @GET("api.php")
    fun getSorted(@Query("type") type:String):Call<Bitmap>
}