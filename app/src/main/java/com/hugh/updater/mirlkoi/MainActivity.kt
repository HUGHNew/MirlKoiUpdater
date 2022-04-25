package com.hugh.updater.mirlkoi

import android.Manifest
import android.annotation.SuppressLint
import android.app.WallpaperManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.DateRange
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.contentValuesOf
import androidx.core.content.edit
import androidx.core.view.GestureDetectorCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.hugh.updater.mirlkoi.databinding.ActivityMainBinding
import com.hugh.updater.mirlkoi.ui.compose.MomentDialog
import com.hugh.updater.mirlkoi.ui.compose.click
import com.hugh.updater.mirlkoi.ui.theme.GraySemi
import com.hugh.updater.mirlkoi.ui.theme.MirlKoiUpdaterTheme
import com.hugh.updater.mirlkoi.ui.theme.Purple200
import com.hugh.updater.mirlkoi.util.*
import com.hugh.updater.mirlkoi.vm.MainViewModel
import com.vanpra.composematerialdialogs.datetime.time.timepicker
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class MainActivity : AppCompatActivity() {
    companion object {
        const val tag = "Updater"
        const val PATH = "MirlKoi"
        const val single = "update.jpg"
        const val shared = "cache"
    }

    private lateinit var bind: ActivityMainBinding
    private lateinit var model: MainViewModel
    private val mDetector =  lazy {
        GestureDetectorCompat(this, SlideGestureDetector {
            if (bind.layoutDrawer.isDrawerOpen(model.DrawerGravity)) {
                bind.layoutDrawer.closeDrawer(model.DrawerGravity)
                L.v(tag, "Slide the Screen CLOSE DRAWER")
            } else {
                bind.layoutDrawer.openDrawer(model.DrawerGravity)
                L.v(tag, "Slide the Screen OPEN DRAWER")
            }
        })
    }

    // region permissions
    private val requestReadLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                model.storageRead = true
                runOnUiThread { setAppPreview(bind.layoutDrawer, model.flag) }
            } else {
                L.w(tag, "failed to get permission and failed to fetch current wallpaper")
                runOnUiThread { showToast("未授权，获取当前壁纸失败", yOff = model.toastYOff) }
            }
        }

    private fun requestStorageRead(callback: () -> Unit) {
        if (!model.storageRead) {
            requestReadLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        } else {
            callback()
        }
    }

    private fun checkPermissions() {
        model.storageRead =
            checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        L.d(tag, "read:${model.storageRead}")
    }
    // endregion
    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bind.root)
        model = ViewModelProvider(this)[MainViewModel::class.java]
        checkPermissions()
        loadSettings()
        bind.drawer.setContent {
            MirlKoiUpdaterTheme {
                Column(verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier
                    .background(color = GraySemi)) {
                    val labelTag = remember { mutableStateOf(findLabelsKey(model.api)) }
                    L.d(tag, "url:${model.api}")
                    ApiLabels.keys.forEach { label ->
                        val onClick = {
                            labelTag.value = label
                            model.api = ApiLabels[label]!!
                            L.v(tag, "click $label|api:${model.api}")
                        }
                        Row(modifier = Modifier
                            .align(Alignment.Start)
                            .click(onClick = onClick)
                            .clip(RoundedCornerShape(8.dp))
                            .fillMaxWidth()
                        ) {
                            RadioButton(
                                selected = labelTag.value == label,
                                onClick = onClick,
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = Purple200,
                                    unselectedColor = Color.Cyan
                                )
                            )
                            Column(modifier = Modifier
                                .wrapContentHeight()
                                .fillMaxWidth()
                                .align(Alignment.CenterVertically)
                                ) {
                                Text(
                                    text = label,
                                    fontSize = 20.sp,
                                    color = Color.Black,
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                )
                            }
                        }
                    }
                    val autoUpdate = remember { mutableStateOf(false) }
                    val dialogState = rememberMaterialDialogState()

                    MomentDialog(dialogState) {
                        timepicker { time ->
                            // Do stuff with java.time.LocalTime object which is passed in
                        }
                    }
                    Button(onClick = {
                        // https://developer.android.com/reference/kotlin/androidx/compose/material/package-summary#AlertDialog(kotlin.Function0,kotlin.Function0,androidx.compose.ui.Modifier,kotlin.Function0,kotlin.Function0,kotlin.Function0,androidx.compose.ui.graphics.Shape,androidx.compose.ui.graphics.Color,androidx.compose.ui.graphics.Color,androidx.compose.ui.window.DialogProperties)
                        MaterialAlertDialogBuilder(this@MainActivity)
//                            .setTitle(resources.getString(R.string.title))
                            .setTitle("定时模式选择")
                            .setItems(arrayOf("按时间点","按间隔")) { _, which ->
                                L.i(tag," $which")
                                when(which){
                                    0 ->{
                                        dialogState.show()
                                    }
                                    1 ->{}
                                }
                            }
                            .show()
                        // a select dialog and two dialogs
                    },
                    colors = ButtonDefaults.buttonColors(
                        contentColor = if(autoUpdate.value){Color.Cyan}else{Color.Gray},
                        backgroundColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .align(Alignment.CenterHorizontally)
                        .background(color = Color.Transparent)
                    ) {
                        Icon(Icons.Sharp.DateRange, contentDescription = "Timer Button",
                            modifier = Modifier.fillMaxSize(1.5f))
                    }
                }
            }
        }
        buttonsAction()

//        val request = OneTimeWorkRequest.Builder(UpdateWork::class.java).build()
//        WorkManager.getInstance(this).enqueue(request)
        // gesture here
        bind.layoutDrawer.setOnTouchListener { _, event ->
            mDetector.value.onTouchEvent(event)
        }
        L.d(tag, "Screen width:${getDisplayScale(" height:")}")

        model.bitmap = try {
                setAppPreview(bind.layoutDrawer,openFileInput(single))
            }catch (e:Exception){ null }
    }

    override fun onDestroy() {
        super.onDestroy()
        saveSettings()
        L.d(tag, "OnDestroy changes saved")
    }

    // region shared preference
    private fun saveSettings() {
        getSharedPreferences(shared, MODE_PRIVATE).edit {
            putBoolean("save", model.alwaysSave)
            putString("url", model.api)
            putInt("flag", model.flag)
            L.d(tag, "save:${model.alwaysSave}\turl:${model.api}")
        }
    }

    private fun loadSettings() {
        val prefs = getSharedPreferences(shared, MODE_PRIVATE)
        model.alwaysSave = prefs.getBoolean("save", false)
        // api
        model.api = prefs.getString("url", "mp") ?: "mp"
        // wallpaper mode
        model.flag = prefs.getInt("flag", 1) // FLAG_SYSTEM === 1

        // init button check status
        bind.wpLock.isChecked = (model.flag and 2) == 2
        bind.wpHome.isChecked = (model.flag and 1) == 1
        bind.wpSave.isChecked = model.alwaysSave
    }

    // endregion
    private fun buttonsAction() {
        bind.wpSave.setOnCheckedChangeListener { _, isChecked ->
            model.alwaysSave = isChecked
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
                    L.d(tag, "api:${model.api} -- downloaded")
                }
                if (model.bitmap == null) {
                    L.e(tag, "error when get image")
                } else {
                    runOnUiThread { setAppPreview(bind.layoutDrawer, model.bitmap) }
                    model.scope.launch {
                        if (model.alwaysSave) {
                            saveImage2Gallery("auto mode:save image to gallery")
                        }
                    }
                    model.scope.launch {
                        model.bitmap?.saveToLocal(openFileOutput(single, Context.MODE_PRIVATE)) {
                            L.d(tag, "get fd downloaded this image")
                        }
                    }
                    model.scope.launch {
                        val width = getDisplayWidth()
                        val height = getDisplayHeight()
                        L.d(tag, "width:$width,height:$height")
                        model.bitmap = model.bitmap?.saveToWallpaper(
                            getSystemService(Context.WALLPAPER_SERVICE) as WallpaperManager,
                            model.flag, Rect(0, 0, width, height)
                        ) {
                            L.d(
                                tag,
                                "bitmap width:${model.bitmap!!.width}; height:${model.bitmap!!.height}"
                            )
                            L.d(tag, "switch wallpaper")
                        }
                    }
                }
            }
        }
    }

    private fun getExternalImageOutputStream(
        filename: String,
        MIME: String = "image/jpeg"
    ): OutputStream? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = contentValuesOf(
                MediaStore.MediaColumns.DISPLAY_NAME to filename,
                MediaStore.MediaColumns.MIME_TYPE to MIME,
                MediaStore.MediaColumns.RELATIVE_PATH to Environment.DIRECTORY_PICTURES + "/" + PATH
            )
            //Inserting the contentValues to contentResolver and getting the Uri
            val imageUri: Uri? =
                contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            L.d(tag, imageUri.toString())
            imageUri?.let { contentResolver.openOutputStream(it) }
        } else {
            val path =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/" + PATH)
            L.d(tag, "use API before Q|$path")
            val image = File(path, filename)
            FileOutputStream(image)
        }
    }

    private fun saveImage2Gallery(msg: String) {
        getExternalImageOutputStream(System.currentTimeMillis().toString())?.let {
            model.bitmap?.saveToGallery(it) {
                L.d(tag, msg)
            }
        }
    }
}