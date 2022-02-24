package com.hugh.wallpaperupdater

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Environment.DIRECTORY_PICTURES
import android.os.StrictMode
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.contentValuesOf
import java.io.*
import java.net.URL
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.net.ssl.HttpsURLConnection
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    companion object{
        const val single = "updater.jpg"
        const val tag = "Updater"
        const val storage = "api"
        val apis = listOf("https://api.btstu.cn/sjbz/api.php?method=mobile",
            "https://picsum.photos/1080/1920",
            "https://iw233.cn/API/mp.php")
        @RequiresApi(Build.VERSION_CODES.O)
        public fun getDateTimeFilename(pattern : String):String=DateTimeFormatter.ofPattern(pattern).format(LocalDateTime.now())
    }
    private lateinit var api : String
    private val mApis = ArrayList<String>()
    private lateinit var editor: EditText
    private lateinit var dropdown : Spinner
    private lateinit var add : Button
    private lateinit var apply : Button
    private lateinit var copy : Button
    private lateinit var remove : Button
    private lateinit var up : Button
    private lateinit var main : ConstraintLayout

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().permitAll().build())
        supportActionBar?.hide()
        initViews()
        initSpinner()
        initButtons()
        setAppPreview()
    }

    override fun onDestroy() {
        super.onDestroy()
        saveUrlList()
        Log.d(tag,"OnDestroy changes saved")
    }
    private fun initViews(){
        editor = findViewById(R.id.add_url)
        dropdown = findViewById(R.id.url_list)
        add = findViewById(R.id.add_button)
        apply = findViewById(R.id.apply_button)
        copy = findViewById(R.id.copy_button)
        remove = findViewById(R.id.rm_button)
        up = findViewById(R.id.update_button)
        main = findViewById(R.id.layout)
    }
    private fun initSpinner(){
        // region dropdown list
        dropdown.adapter = loadUrlList()
        dropdown.onItemSelectedListener=object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                api = mApis[pos]
            }

            override fun onNothingSelected(view: AdapterView<*>?) {
                dropdown.setSelection(0)
                api = mApis[0]
            }
        }
        // endregion
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun initButtons(){
        // region url buttons
        add.setOnClickListener {
            if(editor.text.isNotEmpty() and (editor.text.toString() !in mApis)){
                mApis.add(editor.text.toString())
            }
        }
        apply.setOnClickListener {
            if(editor.text.isNotEmpty()){
                mApis[dropdown.selectedItemPosition]=editor.text.toString()
            }
        }
        copy.setOnClickListener {
            editor.setText(api)
            Log.d(tag,"copy url to EditText")
        }
        remove.setOnClickListener {
            if(mApis.size==1){
                Toast.makeText(this,"You can't remove the last one URL!",Toast.LENGTH_SHORT).show()
            }else{
                val idx =
                    if(dropdown.selectedItemPosition!=mApis.size-1) dropdown.selectedItemPosition
                    else mApis.size-2
                mApis.remove(api)
                dropdown.setSelection(idx)
                api = mApis[idx]
            }
        }
        // endregion
        up.setOnClickListener {
            val th = thread{
                val bitmap = download(api)
                bitmap?.saveToLocal(openFileOutput(single, Context.MODE_PRIVATE)){
                    Log.d(tag,"get fd downloaded this image")
                }
                val os = getExternalImageOutputStream(getDateTimeFilename("yy_MM_dd_HH_mm_ss"))
                os?.let { bitmap?.saveToGallery(os){Log.d(tag,"save image to gallery")} }
                bitmap?.saveToWallpaper(getSystemService(Context.WALLPAPER_SERVICE) as WallpaperManager){
                    Log.d(tag,"switch wallpaper")
                }

                runOnUiThread { setAppPreview(true) }
            }
        }
    }
    private fun setAppPreview(directSet : Boolean = false){
        if(directSet or internalFileExists(single)){
            main.background = Drawable.createFromStream(openFileInput(single),null)
        }
    }
    private fun download(url:String): Bitmap? {
        var bitmap : Bitmap?
        with(URL(url).openConnection() as HttpsURLConnection){
            doInput = true
            requestMethod = "GET"
            connect()
            bitmap = try {
                Log.d(tag,"Downloading image")
                BitmapFactory.decodeStream(inputStream)
            } catch (e:IOException){
                Log.d(tag,"Error when downloading image : ${e.message}")
                null
            }
        }
        return bitmap
    }
    private fun getExternalImageOutputStream(filename : String,MIME:String = "image/jpeg"):OutputStream? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = contentValuesOf (
                MediaStore.MediaColumns.DISPLAY_NAME to filename,
                MediaStore.MediaColumns.MIME_TYPE to MIME,
                MediaStore.MediaColumns.RELATIVE_PATH to DIRECTORY_PICTURES
            )
            //Inserting the contentValues to contentResolver and getting the Uri
            val imageUri: Uri? =
                contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            Log.d(tag,imageUri.toString())
            imageUri?.let { contentResolver.openOutputStream(it) }
        } else {
            val path = Environment.getExternalStoragePublicDirectory(DIRECTORY_PICTURES)
            Log.d(tag,"use API before Q|$path")
            val image = File(path,filename)
            FileOutputStream(image)
        }
    }
    private fun loadUrlList():ArrayAdapter<String>{
        if (!internalFileExists(storage)){
            createDefaultUrlFile()
            mApis.addAll(apis)
        }
        else{
            InputStreamReader(openFileInput(storage)).use {
                mApis.addAll(it.readLines())
            }
        }
        return ArrayAdapter(this,android.R.layout.simple_spinner_item,mApis).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
    }
    private fun saveUrlList() = saveUrls(storage,mApis)
    private fun internalFileExists(file: String):Boolean = getFileStreamPath(file).exists()
    private fun createDefaultUrlFile() = saveUrls(storage,apis)
    private fun saveUrls(file:String,urls:List<String>){
        OutputStreamWriter(openFileOutput(file, MODE_PRIVATE)).use {
            for (api in urls) {
                it.appendLine(api)
            }
        }
    }
}