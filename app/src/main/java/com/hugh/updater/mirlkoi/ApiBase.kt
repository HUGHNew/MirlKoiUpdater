package com.hugh.updater.mirlkoi

// region APIs
enum class ImageType{
    Recent,Random,Top,Gallery,Silver,Furry,PC,Mobile
}
val ApiLabels = listOf(
    "最近更新",
    "随机壁纸",
    "推荐壁纸",
    "星空壁纸",
    "银发壁纸",
    "兽耳壁纸",
    "竖屏壁纸",
    "横屏壁纸",
)
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