package com.hugh.MirlKoiUpdater

import android.app.Activity
import android.content.Context
import androidx.core.content.edit

const val shared = "cache"
const val key = "api"
fun Activity.saveUrl(url:String){
    getSharedPreferences(shared, Context.MODE_PRIVATE).edit {
        putString(key,url)
    }
}
fun Activity.loadUrl():String{
    return getSharedPreferences(shared, Context.MODE_PRIVATE).getString(key,"")?:""
}