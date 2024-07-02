package xyz.haloai.haloai_android_productivity.di

import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import xyz.haloai.haloai_android_productivity.services.AssistantModeFunctions
import xyz.haloai.haloai_android_productivity.services.repository.AssistantModeFunctionsImpl
import xyz.haloai.haloai_android_productivity.services.repository.AssistantModeFunctionsRepository
import xyz.haloai.haloai_android_productivity.ui.viewmodel.AssistantModeFunctionsViewModel

val assistantModeFunctionsModule = module {

    single { AssistantModeFunctions (androidContext()) }
    single<AssistantModeFunctionsRepository>  { AssistantModeFunctionsImpl(get()) }
    viewModel { AssistantModeFunctionsViewModel(get()) }
}