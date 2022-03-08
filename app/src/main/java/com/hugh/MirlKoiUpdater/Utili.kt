package com.hugh.MirlKoiUpdater

import android.app.Activity
import android.content.Context
import androidx.core.content.edit

const val shared = "cache"
const val key = "api"
const val ifSave = "ifSave"
fun Activity.saveUrl(url:String){
    getSharedPreferences(shared, Context.MODE_PRIVATE).edit {
        putString(key,url)
    }
}
fun Activity.loadUrl():String{
    return getSharedPreferences(shared, Context.MODE_PRIVATE).getString(key,"")?:""
}
fun Activity.saveSettings(save:Boolean){
    getSharedPreferences(shared, Context.MODE_PRIVATE).edit {
        putBoolean(ifSave,save)
    }
}
fun Activity.loadSettings():Boolean{
    return getSharedPreferences(shared, Context.MODE_PRIVATE).getBoolean(ifSave,false)
}