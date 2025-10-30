package xyz.haloai.haloai_android_productivity.di

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import xyz.haloai.haloai_android_productivity.services.LocalLlmService
import xyz.haloai.haloai_android_productivity.services.ModelDownloadService

val localLlmModule = module {
    single { LocalLlmService(androidContext()) }
    single { ModelDownloadService(androidContext()) }
}
