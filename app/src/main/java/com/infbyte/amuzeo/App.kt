package com.infbyte.amuzeo

import android.app.Application
import com.infbyte.amuzeo.di.playbackModule
import com.infbyte.amuzeo.di.repoModule
import com.infbyte.amuzeo.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.koinApplication

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        koinApplication {
            startKoin {
                androidContext(this@App)
                modules(repoModule, viewModelModule, playbackModule)
            }
        }
    }
}
