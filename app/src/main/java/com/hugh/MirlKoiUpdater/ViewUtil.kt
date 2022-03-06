package com.hugh.MirlKoiUpdater

import android.Manifest
import android.app.Activity
import android.app.WallpaperManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.hardware.display.DisplayManager
import android.os.Build
import android.view.Display
import android.view.ViewGroup
import androidx.core.content.getSystemService
import androidx.core.graphics.drawable.toDrawable

fun Activity.setAppPreview(layout:ViewGroup,image : Bitmap? = null){
    if(this.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
        !=PackageManager.PERMISSION_GRANTED){
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                MainActivity.READ_STORAGE)
    }else{
        layout.background =
            image?.toDrawable(resources)
                ?: (getSystemService(Context.WALLPAPER_SERVICE) as WallpaperManager).drawable
    }
}

fun Activity.getDisplayWidth():Int{
    return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
        getSystemService<DisplayManager>()?.getDisplay(Display.DEFAULT_DISPLAY)
    }else{
        windowManager.defaultDisplay
    }?.width ?:1080
}

fun Activity.getDisplayHeight():Int{
    return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
        getSystemService<DisplayManager>()?.getDisplay(Display.DEFAULT_DISPLAY)
    }else{
        windowManager.defaultDisplay
    }?.height ?:2259
}

fun Activity.getDisplayScale(sep:String = "/"):String  =
    "${getDisplayWidth()}$sep${getDisplayHeight()}"