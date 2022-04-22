package com.hugh.updater.mirlkoi.util

// region APIs
enum class ImageType{
    Recent,Random,Top,Gallery,Silver,Furry,PC,Mobile
}
val ApiLabels = mapOf(
    "最近更新" to "iw233",
    "随机壁纸" to "random",
    "推荐壁纸" to "top",
    "星空壁纸" to "xing",
    "银发壁纸" to "yin",
    "兽耳壁纸" to "cat",
    "竖屏壁纸" to "mp",
    "横屏壁纸" to "pc",
)

inline fun findLabelsKey(value:String,seq:Int = 1):String{
    var times = 0
    for (entry in ApiLabels.entries){
        if(entry.value == value){
            if(++times==seq){
                return entry.key
            }
        }
    }
    return ""
}

val Type2Api = mapOf(
    ImageType.Recent to "iw233",
    ImageType.Random to "random",
    ImageType.Top to "top",
    ImageType.Gallery to "xing",
    ImageType.Silver to "yin",
    ImageType.Furry to "cat",
    ImageType.Mobile to "mp",
    ImageType.PC to "pc",
)
// endregion
const val BASE_URL = "https://iw233.cn"