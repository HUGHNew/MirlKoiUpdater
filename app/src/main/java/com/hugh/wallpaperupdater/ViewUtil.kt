package com.hugh.wallpaperupdater

import android.app.Activity
import android.graphics.drawable.Drawable
import android.hardware.display.DisplayManager
import android.os.Build
import android.view.Display
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService
import java.io.OutputStreamWriter

fun Activity.internalFileExists(file: String):Boolean = getFileStreamPath(file).exists()

fun Activity.setAppPreview(layout:ViewGroup,directSet : Boolean = false){
    if(directSet or internalFileExists(MainActivity.single)){
        layout.background = Drawable.createFromStream(openFileInput(MainActivity.single),null)
    }
}
fun Activity.saveUrls(file:String, urls:List<String>, block:(OutputStreamWriter)->Unit={}){
    OutputStreamWriter(openFileOutput(file, AppCompatActivity.MODE_PRIVATE)).use {
        for (api in urls) {
            it.appendLine(api)
        }
        block(it)
    }
}

fun Activity.getDisplayScale(delim:String = "/"):String  =
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
        getSystemService<DisplayManager>()?.getDisplay(Display.DEFAULT_DISPLAY)
    }else{
        windowManager.defaultDisplay
    }?.let{
        "${it.width}$delim${it.height}"
    }?:"1080${delim}1920"