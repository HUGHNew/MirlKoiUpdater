package com.hugh.updater.mirlkoi.vm

import android.app.WallpaperManager
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job


class MainViewModel:ViewModel() {
    val toastYOff = 50
    var api : String = "mp"
    var bitmap : Bitmap? = null
    var storageRead = false
    var flag : Int = WallpaperManager.FLAG_SYSTEM
    private val mainCoroutineJob = Job()
    val scope = CoroutineScope(mainCoroutineJob)
}