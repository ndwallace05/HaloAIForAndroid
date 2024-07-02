package xyz.haloai.haloai_android_productivity.di

import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import xyz.haloai.haloai_android_productivity.services.OpenAIService
import xyz.haloai.haloai_android_productivity.services.repository.OpenAIRepository
import xyz.haloai.haloai_android_productivity.services.repository.OpenAIRepositoryImpl
import xyz.haloai.haloai_android_productivity.ui.viewmodel.OpenAIViewModel

val openAIModule = module {
    single { OpenAIService(androidContext()) }
    single<OpenAIRepository>  { OpenAIRepositoryImpl(get()) }
    viewModel { OpenAIViewModel(get()) }
}