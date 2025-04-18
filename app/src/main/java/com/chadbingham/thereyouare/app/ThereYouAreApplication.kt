package com.chadbingham.thereyouare.app

import android.app.Application
import timber.log.Timber

class ThereYouAreApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }
}