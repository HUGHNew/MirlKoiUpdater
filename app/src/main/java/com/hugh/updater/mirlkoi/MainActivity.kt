package com.hugh.updater.mirlkoi

import android.Manifest
import android.app.WallpaperManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.contentValuesOf
import androidx.core.content.edit
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProvider
import com.hugh.updater.mirlkoi.databinding.ActivityMainBinding
import com.hugh.updater.mirlkoi.util.*
import com.hugh.updater.mirlkoi.vm.MainViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class MainActivity : AppCompatActivity() {
    companion object{
        const val tag = "Updater"
        const val PATH = "MirlKoi"
        const val single = "update.jpg"
        const val shared = "cache"
        // region api region
        val apis = listOf(
            "pc",
            "mp",
            "random",
            "top",
            "iw233",
        )
        val apiRadios = mapOf(
            R.id.api_pc to apis[0],
            R.id.api_mobile to apis[1],
            R.id.api_random to apis[2],
            R.id.api_recommend to apis[3],
            R.id.api_recent to apis[4],
        )
        val apiDescId = mapOf(
            R.id.api_pc to R.string.api_pc,
            R.id.api_mobile to R.string.api_mp,
            R.id.api_random to R.string.api_random,
            R.id.api_recommend to R.string.api_recommend,
            R.id.api_recent to R.string.api_recent,
        )
        // endregion
    }
    private lateinit var bind : ActivityMainBinding
    private lateinit var model: MainViewModel
    // region permissions
    private val requestReadLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){
        if (it){
            model.storageRead = true
            runOnUiThread { setAppPreview(bind.layoutDrawer, model.flag) }
        }else{
            L.w(tag,"failed to get permission and failed to fetch current wallpaper")
            runOnUiThread { showToast("未授权，获取当前壁纸失败", yOff = model.toastYOff) }
        }
    }
    private fun requestStorageRead(callback:()->Unit){
        if (!model.storageRead){
            requestReadLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }else{
            callback()
        }
    }
    private fun checkPermissions(){
        model.storageRead = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        L.d(tag,"read:${model.storageRead}")
    }
    // endregion
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bind.root)
        model = ViewModelProvider(this)[MainViewModel::class.java]

        checkPermissions()
        loadSettings()
        buttonsAction()
        L.d(tag,"Screen width:${getDisplayScale(" height:")}")

        requestStorageRead {
            setAppPreview(bind.layoutDrawer, model.flag)
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        saveSettings()
        L.d(tag,"OnDestroy changes saved")
    }

    // region shared preference
    private fun saveSettings(){
        getSharedPreferences(shared, MODE_PRIVATE).edit{
            // todo save
            putBoolean("save",bind.save.isChecked)
            putInt("url",apis.indexOf(model.api))
            putInt("flag", model.flag)
            L.d(tag,"save:${bind.save.isChecked}\nurl pos:${apis.indexOf(model.api)}")
        }
    }
    private fun loadSettings(){
        val prefs = getSharedPreferences(shared, MODE_PRIVATE)
        bind.save.isChecked = prefs.getBoolean("save",false)
        // api
        val selected = prefs.getInt("url",1)
        model.api = apis[selected]
        bind.apiGroup.check(bind.apiGroup.getChildAt(selected).id) // set selected
        // wallpaper mode
        model.flag = prefs.getInt("flag",1) // FLAG_SYSTEM === 1
        bind.wpLock.isChecked = (model.flag and 2)==2
        bind.wpHome.isChecked = (model.flag and 1)==1
    }
    // endregion
    private fun buttonsAction(){
        bind.apiGroup.setOnCheckedChangeListener { _, id ->
            model.api = apiRadios[id]!!
            showToast("切换壁纸选择:${getString(apiDescId[id]!!)}", yOff = model.toastYOff)
            L.d(tag,"button clicked ${model.api}")
        }

        bind.wpHome.setOnClickListener {
            model.flag = model.flag xor WallpaperManager.FLAG_SYSTEM
        }
        bind.wpLock.setOnClickListener {
            model.flag = model.flag xor WallpaperManager.FLAG_LOCK
        }
        bind.downloadButton.setOnClickListener {
            model.scope.launch {
                saveImage2Gallery("Button action : save image to gallery")
            }
        }
        bind.updateButton.setOnClickListener {
            OkHttpUtil.api(model.api) { _, response ->
                response.body()?.let {
                    model.bitmap = BitmapFactory.decodeStream(it.byteStream())
                    L.d(tag,"downloaded")
                }
                if (model.bitmap == null){
                    L.e(tag,"error when get image")
                }else{
                    runOnUiThread { setAppPreview(bind.layoutDrawer,model.bitmap) }
                    model.scope.launch {
                        if(bind.save.isChecked){
                            saveImage2Gallery("auto mode:save image to gallery")
                        }
                    }
                    model.scope.launch {
                        model.bitmap?.saveToLocal(openFileOutput(single, Context.MODE_PRIVATE)){
                            L.d(tag,"get fd downloaded this image")
                        }
                    }
                    model.scope.launch {
                        val width = getDisplayWidth()
                        val height = getDisplayHeight()
                        L.d(tag,"width:$width,height:$height")
                        model.bitmap = model.bitmap?.saveToWallpaper(
                            getSystemService(Context.WALLPAPER_SERVICE) as WallpaperManager,
                            model.flag, Rect(0,0,width,height)){
                            L.d(tag,"bitmap width:${model.bitmap!!.width}; height:${model.bitmap!!.height}")
                            L.d(tag,"switch wallpaper")
                        }
                    }
                }
            }
        }
        // region drawer buttons
        bind.settings.setOnClickListener {
            bind.layoutDrawer.openDrawer(GravityCompat.END)
        }
        // endregion
    }
    private fun getExternalImageOutputStream(filename : String,MIME:String = "image/jpeg"): OutputStream? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = contentValuesOf (
                MediaStore.MediaColumns.DISPLAY_NAME to filename,
                MediaStore.MediaColumns.MIME_TYPE to MIME,
                MediaStore.MediaColumns.RELATIVE_PATH to Environment.DIRECTORY_PICTURES+"/"+PATH
            )
            //Inserting the contentValues to contentResolver and getting the Uri
            val imageUri: Uri? =
                contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            L.d(tag,imageUri.toString())
            imageUri?.let { contentResolver.openOutputStream(it) }
        } else {
            val path = Environment.
                getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES+"/"+PATH)
            L.d(tag,"use API before Q|$path")
            val image = File(path,filename)
            FileOutputStream(image)
        }
    }
    private fun saveImage2Gallery(msg:String){
        getExternalImageOutputStream(System.currentTimeMillis().toString())?.let {
            model.bitmap?.saveToGallery(it){
                L.d(tag,msg)
            }
        }
    }
}