package com.hugh.MirlKoiUpdater

import android.app.WallpaperManager
import android.graphics.Bitmap
import android.graphics.Rect
import java.io.OutputStream

inline fun saveBitmapToFile(bitmap: Bitmap,os: OutputStream,block : ()->Unit={}):Boolean=
    try {
        os.use {
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,it)
        }
        block()
        true
    }catch (ignored:Exception){
        false
    }

inline fun Bitmap.saveToLocal(os: OutputStream, block : ()->Unit={}):Boolean{
    return saveBitmapToFile(this,os,block)
}

inline fun Bitmap.saveToGallery(os: OutputStream,block : ()->Unit={}):Boolean{
    return saveBitmapToFile(this,os,block)
}
inline fun Bitmap.saveToWallpaper(wallMan: WallpaperManager,mode:Int,
                                  rect:Rect?=null, block: () -> Unit):Bitmap=
    with(this){
//        wallMan.suggestDesiredDimensions(width,height)
        var bitmap = this
        if(rect!=null){
            bitmap = Bitmap.createScaledBitmap(this,rect.width(),rect.height(),true)
        }
        wallMan.setBitmap(bitmap,null,false,mode)
        block()
        bitmap
    }
