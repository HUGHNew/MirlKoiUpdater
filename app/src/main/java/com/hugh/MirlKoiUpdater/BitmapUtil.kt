package com.hugh.MirlKoiUpdater

import android.app.WallpaperManager
import android.graphics.Bitmap
import android.graphics.Rect
import com.hugh.MirlKoiUpdater.MainActivity.Companion.logD
import java.io.OutputStream
import kotlin.math.min

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
// vertical only
fun getScaleRatio(src_w:Int,src_h:Int,dst_w:Int,dst_h:Int):Int{
    return if(src_w<dst_w || src_h<dst_h){
        1
    }else{
        min(src_h/dst_h,src_w/dst_w)
    }
}
fun Bitmap.getFitBitmap(Width:Int,Height:Int):Bitmap{
    val bitmap = if(width>height){
        val w = height * Width / Height
        val x = (width-w)/2
        Bitmap.createBitmap(this,x,0,w,height)
    }else{this}
    val bm = Bitmap.createScaledBitmap(bitmap,Width,Height,true)
    logD("create bitmap","width:${bm.width}|height:${bm.height}")
    return bm
}
inline fun Bitmap.saveToWallpaper(wallMan: WallpaperManager,mode:Int,
                                  rect:Rect?=null, block: () -> Unit):Bitmap=
    with(this){
        block()
        if(mode == 0){
            this
        }else{
            val bitmap = if(rect!=null){
                this.getFitBitmap(rect.width(),rect.height())
            }else{
                Bitmap.createScaledBitmap(this,width,height,false)
            }
            wallMan.suggestDesiredDimensions(bitmap.width,bitmap.height)
            wallMan.setBitmap(bitmap,null,false,mode)
            bitmap
        }
    }
