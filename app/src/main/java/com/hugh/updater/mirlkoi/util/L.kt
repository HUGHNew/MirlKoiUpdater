package com.hugh.updater.mirlkoi.util

import android.util.Log

object L {
    private val level = Level.Error

    enum class Level {
        Shut, Verbose, Debug, Info, Warn, Error
    }

    fun v(tag: String, msg: String) {
        if (level >= Level.Verbose) {
            Log.v(tag, msg)
        }
    }

    fun d(tag: String, msg: String) {
        if (level >= Level.Debug) {
            Log.d(tag, msg)
        }
    }

    fun i(tag: String, msg: String) {
        if (level >= Level.Info) {
            Log.i(tag, msg)
        }
    }

    fun w(tag: String, msg: String) {
        if (level >= Level.Warn) {
            Log.w(tag, msg)
        }
    }

    fun e(tag: String, msg: String) {
        if (level >= Level.Error) {
            Log.e(tag, msg)
        }
    }
}