package com.hugh.updater.mirlkoi

import android.view.GestureDetector
import android.view.MotionEvent

class SlideGestureDetector(val action:()->Unit):GestureDetector.SimpleOnGestureListener() {
    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent?,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        action()
        return true
    }
}