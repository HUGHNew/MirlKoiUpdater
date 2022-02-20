package com.hugh.wallpaperupdater

import android.app.WallpaperManager
import android.graphics.Bitmap
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
inline fun Bitmap.saveToWallpaper(wallMan: WallpaperManager, block: () -> Unit)=
    with(this){
        wallMan.suggestDesiredDimensions(width,height)
        wallMan.setBitmap(this)
        block()
}