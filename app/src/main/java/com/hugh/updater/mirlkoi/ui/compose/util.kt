package com.hugh.updater.mirlkoi.ui.compose

import androidx.compose.foundation.clickable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.semantics.Role

const val VIEW_CLICK_INTERVAL_TIME = 200

// avoid click repeatedly
inline fun Modifier.click(
    time: Int = VIEW_CLICK_INTERVAL_TIME,
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null,
    crossinline onClick: () -> Unit
): Modifier = composed {
    var lastClickTime = remember { 0L }//使用remember函数记录上次点击的时间
    clickable(enabled, onClickLabel, role) {
        val currentTimeMillis = System.currentTimeMillis()
        if (currentTimeMillis - time >= lastClickTime) {//判断点击间隔,如果在间隔内则不回调
            onClick()
            lastClickTime = currentTimeMillis
        }
    }
}