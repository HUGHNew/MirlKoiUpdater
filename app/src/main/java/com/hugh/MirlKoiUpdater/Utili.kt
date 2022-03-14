package com.hugh.MirlKoiUpdater

import android.app.Activity
import android.content.Context
import androidx.core.content.edit

const val shared = "cache"
const val key = "api"
const val ifSave = "ifSave"
fun Activity.saveUrl(url:Int){
    getSharedPreferences(shared, Context.MODE_PRIVATE).edit {
        putInt(key,url)
    }
}
fun Activity.loadUrl():Int{
    return getSharedPreferences(shared, Context.MODE_PRIVATE).getInt(key,1)
}
fun Activity.saveSettings(save:Boolean){
    getSharedPreferences(shared, Context.MODE_PRIVATE).edit {
        putBoolean(ifSave,save)
    }
}
fun Activity.loadSettings():Boolean{
    return getSharedPreferences(shared, Context.MODE_PRIVATE).getBoolean(ifSave,false)
}