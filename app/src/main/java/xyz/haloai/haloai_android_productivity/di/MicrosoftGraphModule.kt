package xyz.haloai.haloai_android_productivity.di

import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import xyz.haloai.haloai_android_productivity.services.MicrosoftGraphService
import xyz.haloai.haloai_android_productivity.services.repository.MicrosoftGraphRepository
import xyz.haloai.haloai_android_productivity.services.repository.MicrosoftGraphRepositoryImplementation
import xyz.haloai.haloai_android_productivity.ui.viewmodel.MicrosoftGraphViewModel

val microsoftGraphModule = module {
    single { MicrosoftGraphService(androidContext()) }
    single<MicrosoftGraphRepository>  { MicrosoftGraphRepositoryImplementation(get()) }
    viewModel { MicrosoftGraphViewModel(get()) }
}