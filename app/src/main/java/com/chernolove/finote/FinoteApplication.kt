package com.chernolove.finote

import android.app.Application
import com.chernolove.finote.di.finoteModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class FinoteApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@FinoteApplication)
            modules(finoteModule)
        }
    }
}
