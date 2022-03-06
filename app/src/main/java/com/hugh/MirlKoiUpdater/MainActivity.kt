package com.hugh.MirlKoiUpdater

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.view.GravityCompat
import com.hugh.MirlKoiUpdater.databinding.ActivityMainBinding
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.net.ssl.HttpsURLConnection
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    companion object{
        // region permission code
        const val READ_STORAGE = 0
        // endregion

        const val tag = "Updater"
        const val PATH = "MirlKoi"
        const val single = "update.jpg"
        var wallpaperFlag : Int = WallpaperManager.FLAG_SYSTEM
        val apis = listOf(
            "https://iw233.cn/API/mp.php",
            "https://iw233.cn/API/Random.php",
            "https://iw233.cn/API/Mirlkoi.php",
            "https://iw233.cn/API/Mirlkoi-iw233.php",
            "http://iw233.fgimax2.fgnwctvip.com/API/Ghs.php",
        )
        val apiRadios = mapOf(
            R.id.api_mobile to apis[0],
            R.id.api_random to apis[1],
            R.id.api_recom to apis[2],
            R.id.api_recent to apis[3],
            R.id.api_porn to apis[4],
        )
        val apiDescId = mapOf(
            R.id.api_mobile to R.string.api_mp,
            R.id.api_random to R.string.api_random,
            R.id.api_recom to R.string.api_recommend,
            R.id.api_recent to R.string.api_recent,
            R.id.api_porn to R.string.api_porn,
        )
        @RequiresApi(Build.VERSION_CODES.O)
        fun getDateTimeFilename(pattern : String):String=
            DateTimeFormatter.ofPattern(pattern).format(LocalDateTime.now())
    }
    // region api
    private var api : String = apis[0]
    private val mApis = ArrayList<String>()
    // endregion
    private lateinit var bind : ActivityMainBinding
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bind.root)
        
        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().permitAll().build())
        supportActionBar?.hide()


        buttonsAction()
        Log.d(tag,"Screen width:${getDisplayScale(" height:")}")

        setAppPreview(bind.layoutDrawer)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(tag,"OnDestroy changes saved")
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            READ_STORAGE->{
                if(grantResults.isEmpty()){
                    Toast.makeText(this,"没有授权，不能生成应用内壁纸预览",
                        Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun buttonsAction(){
        bind.apiGroup.setOnCheckedChangeListener { _, id ->
            api = apiRadios[id]!!
            Toast.makeText(this,"切换壁纸选择:${getString(apiDescId[id]!!)}",
                Toast.LENGTH_SHORT).show()
            Log.d(tag,"button clicked $api")
        }

        bind.modeGroup.setOnCheckedChangeListener { _, id ->
            wallpaperFlag = when(id){
                R.id.wp_lock -> WallpaperManager.FLAG_LOCK // 2
                R.id.wp_home -> WallpaperManager.FLAG_SYSTEM // 1
                else -> 3 // LOCK | SYSTEM
            }
            Log.d(tag,"wallpaper setting flag:$wallpaperFlag")
        }
        bind.hideChoice.setOnClickListener {
            Log.d(tag,"choice clicked!")
            if (bind.hideChoice.isChecked){
                bind.apiPorn.visibility = View.VISIBLE
                Log.d(tag,"set porn radio visible")
            }else{
                bind.apiPorn.visibility = View.INVISIBLE
                Log.d(tag,"set porn radio invisible")
            }
        }
        bind.updateButton.setOnClickListener {
            thread{
                val bitmap = download(api)
                if(bitmap==null){
                    Log.w(tag,"Don't get wallpaper from API")
                }
                bitmap?.saveToLocal(openFileOutput(single, Context.MODE_PRIVATE)){
                    Log.d(tag,"get fd downloaded this image")
                }
                val width = getDisplayWidth()
                val height = getDisplayHeight()
                Log.d(tag,"width:$width,height:$height")
                bitmap?.saveToWallpaper(
                    getSystemService(Context.WALLPAPER_SERVICE) as WallpaperManager,
                    wallpaperFlag, Rect(0,0,width,height)){
                    Log.d(tag,"switch wallpaper")
                }

                runOnUiThread { setAppPreview(bind.layoutDrawer,bitmap) }
            }
        }
        // region drawer buttons
        bind.settings.setOnClickListener {
            bind.layoutDrawer.openDrawer(GravityCompat.END)
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
            Log.d(tag,"response code:${connector.responseCode}")
            when (connector.responseCode) {
                200 -> {
                    Log.d(tag,"fetch image successfully")
                    redirect = false
                    Log.d(tag,"Downloading image")
                    bitmap = BitmapFactory.decodeStream(connector.inputStream)
                    connector.disconnect()
                }
                in 300..400 -> {
                    Log.d(tag,"resp code:${connector.responseCode}|url:${connector.url}")
                    Log.w(tag,"location:${connector.getHeaderField("Location")}")
                    connector.disconnect()
                    connector = getURLConnection(connector.getHeaderField("Location"))
                }
                in 400..500 -> {
                    runOnUiThread {
                        Toast.makeText(this,
                            "API server is deprecated",Toast.LENGTH_LONG).show()
                    }
                    connector.disconnect()
                    redirect=false
                }
            }
        }while (redirect)
        return bitmap
    }
}