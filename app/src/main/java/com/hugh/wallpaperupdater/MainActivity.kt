package com.hugh.wallpaperupdater

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import androidx.core.content.contentValuesOf
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import java.io.*
import java.net.HttpURLConnection
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
        const val half_url = "https://picsum.photos/" // make size fix screen
        val apis = listOf("https://api.btstu.cn/sjbz/api.php?method=mobile",
            "https://iw233.cn/API/mp.php",
        )
        @RequiresApi(Build.VERSION_CODES.O)
        fun getDateTimeFilename(pattern : String):String=DateTimeFormatter.ofPattern(pattern).format(LocalDateTime.now())
    }
    // region Xml UI
    private lateinit var api : String
    private val mApis = ArrayList<String>()
    private val editor : EditText by lazy { findViewById(R.id.add_url) }
    private val dropdown : Spinner by lazy { findViewById(R.id.url_list) }
    private val add : ImageButton by lazy { findViewById(R.id.add_button) }
    private val remove : ImageButton by lazy { findViewById(R.id.rm_button) }
    private val up : ImageButton by lazy { findViewById(R.id.update_button) }
    private val settings : ImageButton by lazy {findViewById(R.id.settings)}
//    private val main : ConstraintLayout by lazy { findViewById(R.id.layout) }
    private val drawer : DrawerLayout by lazy { findViewById(R.id.layout_drawer) }
    // region on drawer
    private val timer : TimePicker by lazy { findViewById(R.id.time_setter) }
    private val autoUpdater : CheckBox by lazy { findViewById(R.id.updater_trigger) }
    private val updateDaily : RadioButton by lazy { findViewById(R.id.update_daily) }
    private val updatePeriod : RadioButton by lazy { findViewById(R.id.update_period) }
    // endregion
    // endregion

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().permitAll().build())
        supportActionBar?.hide()

        initSpinner()
        initButtons()
        Log.d(tag,"Screen width:${getDisplayScale(" height:")}")

        timer.setIs24HourView(true)
        setAppPreview(drawer)
    }

    override fun onDestroy() {
        super.onDestroy()
        saveUrlList()
        Log.d(tag,"OnDestroy changes saved")
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
                val app = editor.text.toString()
                mApis.add(app)
                Log.d(tag,"url appended: $app")
            }
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
            thread{
                val bitmap = download(api)
                if(bitmap==null){
                    Log.w(tag,"Don't get wallpaper from API")
                }
                bitmap?.saveToLocal(openFileOutput(single, Context.MODE_PRIVATE)){
                    Log.d(tag,"get fd downloaded this image")
                }
                val os = getExternalImageOutputStream(getDateTimeFilename("yy_MM_dd_HH_mm_ss"))
                os?.let { bitmap?.saveToGallery(os){Log.d(tag,"save image to gallery")} }
                bitmap?.saveToWallpaper(getSystemService(Context.WALLPAPER_SERVICE) as WallpaperManager){
                    Log.d(tag,"switch wallpaper")
                }

                runOnUiThread { setAppPreview(drawer,true) }
            }
        }
        // region drawer buttons
        settings.setOnClickListener {
            drawer.openDrawer(GravityCompat.END)
        }
        autoUpdater.setOnClickListener {
            if (autoUpdater.isChecked){
                updateDaily.visibility=View.VISIBLE
                updatePeriod.visibility=View.VISIBLE
                // TODO to set task
            }else{
                updateDaily.visibility=View.INVISIBLE
                updatePeriod.visibility=View.INVISIBLE
                // TODO to unset task
            }
        }
        // endregion
    }
    private fun getURLConnection(url:String):HttpURLConnection{
        val con = if ("https" in url)
            URL(url).openConnection() as HttpsURLConnection
        else URL(url).openConnection() as HttpURLConnection
        return con.apply {
            doInput = true
            requestMethod = "GET"
            instanceFollowRedirects = true
        }
    }
    private fun download(url:String): Bitmap? {
        var bitmap : Bitmap? = null
        Log.d(tag,"Target URL:$url")
        var connector = getURLConnection(url)
        var redirect = true
        do {
            connector.connect()
            if(connector.responseCode == 200){
                Log.d(tag,"fetch image successfully")
                redirect = false
                Log.d(tag,"Downloading image")
                bitmap = BitmapFactory.decodeStream(connector.inputStream)
                connector.disconnect()
            }else{
                Log.d(tag,"resp code:${connector.responseCode}|url:${connector.url}")
                Log.w(tag,"location:${connector.getHeaderField("Location")}")
                connector.disconnect()
                connector = getURLConnection(connector.getHeaderField("Location"))
            }
        }while (redirect)
        return bitmap
    }
    private fun getExternalImageOutputStream(filename : String,MIME:String = "image/jpeg"):OutputStream? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = contentValuesOf (
                MediaStore.MediaColumns.DISPLAY_NAME to filename,
                MediaStore.MediaColumns.MIME_TYPE to MIME,
                MediaStore.MediaColumns.RELATIVE_PATH to "${DIRECTORY_PICTURES}/WUpdater"
            )
            //Inserting the contentValues to contentResolver and getting the Uri
            val imageUri: Uri? =
                contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            Log.d(tag,imageUri.toString())
            imageUri?.let { contentResolver.openOutputStream(it) }
        } else {
            val path = Environment.getExternalStoragePublicDirectory("${DIRECTORY_PICTURES}/WUpdater")
            Log.d(tag,"use API before Q|$path")
            val image = File(path,filename)
            FileOutputStream(image)
        }
    }
    private fun loadUrlList():ArrayAdapter<String>{
        if (!internalFileExists(storage)){
            createDefaultUrlFile()
            mApis.addAll(apis)
            mApis.add(half_url+getDisplayScale())
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
    private fun createDefaultUrlFile() = saveUrls(storage,apis){
        it.appendLine(half_url+getDisplayScale())
    }
}