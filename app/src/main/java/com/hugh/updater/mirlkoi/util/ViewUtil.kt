package com.hugh.updater.mirlkoi.util

import android.annotation.SuppressLint
import android.app.Activity
import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.graphics.drawable.toDrawable
import java.io.InputStream

@SuppressLint("MissingPermission")
fun Activity.setAppPreview(layout:ViewGroup, wpf:Int=WallpaperManager.FLAG_SYSTEM){
    if(wpf==0)return
    val flag = if(wpf==3){
        WallpaperManager.FLAG_SYSTEM
    }else{
        wpf
    }
    with(WallpaperManager.getInstance(this)){
        getWallpaperFile(flag).use {
            layout.background = BitmapFactory
                .decodeFileDescriptor(it.fileDescriptor)
                .toDrawable(resources)
        }
    }
}
fun Activity.setAppPreview(layout:ViewGroup,stream: InputStream):Bitmap{
    return BitmapFactory.decodeStream(stream).apply {
        setAppPreview(layout,this)
    }
}
fun Activity.setAppPreview(layout:ViewGroup,image : Bitmap?){
    image?.let { layout.background =  it.toDrawable(resources) }
}

fun Activity.getDisplayWidth():Int{
    return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
        windowManager.currentWindowMetrics.bounds.width()
    }else{
//        getSystemService<DisplayManager>()?.getDisplay(Display.DEFAULT_DISPLAY)?.width
//        windowManager.defaultDisplay?.width?:1080
        windowManager.defaultDisplay!!.width
    }
}

fun Activity.getDisplayHeight():Int{
    return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
        windowManager.currentWindowMetrics.bounds.height()
    }else{
//        getSystemService<DisplayManager>()?.getDisplay(Display.DEFAULT_DISPLAY)?.height
//        windowManager.defaultDisplay?.height ?:2259
        windowManager.defaultDisplay!!.height
    }
}

fun Activity.getDisplayScale(sep:String = "/"):String  =
    "${getDisplayWidth()}$sep${getDisplayHeight()}"
fun Context.showToast(msg:String, longTime:Boolean = false,
                      toast_gravity: Int = Gravity.BOTTOM, xOff : Int = 0, yOff : Int = 0){
    Toast.makeText(this,msg,
        if(longTime)Toast.LENGTH_LONG
        else Toast.LENGTH_SHORT
    ).apply{
        setGravity(toast_gravity,xOff,yOff)
    }.show()
}