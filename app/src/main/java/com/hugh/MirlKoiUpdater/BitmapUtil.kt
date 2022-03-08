package com.hugh.MirlKoiUpdater

import android.app.WallpaperManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
        block()
        var bitmap = this
        if(rect!=null){
            var x = 0
            var y = 0
            var w = rect.width()
            var h = rect.height()
            if (bitmap.width>w) {
                x = (bitmap.width - w)/2
            }else{
                w = bitmap.width
            }
            if (bitmap.height>h) {
                y = (bitmap.height - h)/2
            }else{
                h = bitmap.height
            }
            bitmap = Bitmap.createBitmap(bitmap,x,y,w,h,null,true)
        }
        wallMan.setBitmap(bitmap,null,false,mode)
        bitmap
    }
