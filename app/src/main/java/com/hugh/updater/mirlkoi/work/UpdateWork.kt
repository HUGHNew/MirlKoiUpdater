package com.hugh.updater.mirlkoi.work

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class UpdateWork(context:Context,params: WorkerParameters,val action:()->Boolean): Worker(context,params) {
    override fun doWork(): Result {
        return if (action()){
            Result.success()
        }else{
            Result.failure()
        }
    }
}