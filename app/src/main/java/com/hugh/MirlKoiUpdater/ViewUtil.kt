package com.hugh.MirlKoiUpdater

import android.Manifest
import android.app.Activity
import android.app.WallpaperManager
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.display.DisplayManager
import android.os.Build
import android.view.Display
import android.view.ViewGroup
import androidx.core.content.getSystemService
import androidx.core.graphics.drawable.toDrawable

fun Activity.askForPermissions(permission:String,requestId:Int,requires:Array<String>){
    if(checkSelfPermission(permission)!=PackageManager.PERMISSION_GRANTED){
        requestPermissions(requires,requestId)
    }
}

fun Activity.setAppPreview(layout:ViewGroup,wpf:Int=WallpaperManager.FLAG_SYSTEM){
    if(wpf==0)return
    val flag = if(wpf==3){
        WallpaperManager.FLAG_SYSTEM
    }else{
        wpf
    }
    askForPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,MainActivity.READ_STORAGE,
        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
    val wm = WallpaperManager.getInstance(this)
    val pfd = wm.getWallpaperFile(flag)
    layout.background = BitmapFactory.decodeFileDescriptor(pfd.fileDescriptor).toDrawable(resources)
    pfd.close()
}
fun Activity.setAppPreview(layout:ViewGroup,image : Bitmap?){
    image?.let { layout.background =  it.toDrawable(resources) }
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