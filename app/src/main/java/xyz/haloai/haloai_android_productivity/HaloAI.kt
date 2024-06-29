package xyz.haloai.haloai_android_productivity

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import xyz.haloai.haloai_android_productivity.di.emailDbModule
import xyz.haloai.haloai_android_productivity.di.gmailModule
import xyz.haloai.haloai_android_productivity.di.scheduleDbModule

class HaloAI: Application()  {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@HaloAI)
            modules(emailDbModule, scheduleDbModule, gmailModule)
        }
    }
}