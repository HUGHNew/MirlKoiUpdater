package com.hugh.updater.mirlkoi.util

import okhttp3.*
import java.io.IOException
import java.util.concurrent.TimeUnit

object OkHttpUtil {
    const val tag = "OkHttpUtil"
    private const val callTimeoutValue = 2L
    val http: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(callTimeoutValue/2,TimeUnit.SECONDS)
        .callTimeout(callTimeoutValue, TimeUnit.SECONDS)
        .followRedirects(true).build()
    fun api(type: ImageType, proc:(Call, Response)->Unit){
        access(addApiSort(type),proc)
    }
    fun api(type: String, proc:(Call, Response)->Unit){
        access(addApiSort(type),proc)
    }
    private fun access(url:String,proc:(Call, Response)->Unit){
        http.newCall(Request.Builder().url(url).build()).succeed(proc)
    }
    private fun Call.succeed(proc:(Call, Response)->Unit){
        this.enqueue(object :Callback{
            override fun onFailure(call: Call, e: IOException) {
                L.e(tag, "$call failed")
                L.e(tag,"exception:${e.cause}|${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                L.d(tag, "response code:${response.code()}")
                proc(call, response)
            }
        })
    }
    private fun addApiSort(type: ImageType):String{
        return "$BASE_URL/api.php?sort=${Type2Api[type]}"
    }
    private fun addApiSort(type: String):String{
        return "$BASE_URL/api.php?sort=$type"
    }
}