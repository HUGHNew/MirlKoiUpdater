package com.hugh.updater.mirlkoi.util

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object HttpsUtil {
    private const val tag = "HttpsUtil"
    private const val BASE_URL = "https://iw233.cn"
    val http: ImageService = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ImageService::class.java)

    fun<T> Call<T>.succeed(resp:(Call<T>,Response<T>)->Unit){
        this.enqueue(object : Callback<T>{
            override fun onResponse(call: Call<T>, response: Response<T>) {
                L.d(tag, "code:${response.code()}")
                when(response.code()){
                    in 300..400 ->{
                        L.w(tag, "redirect")
                        // TODO redirect problem
                    }
                }
                resp(call,response)
            }
            override fun onFailure(call: Call<T>, t: Throwable) {
                L.e(tag, "$call failed")
            }
        })
    }
}