package xyz.haloai.haloai_android_productivity.di

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import xyz.haloai.haloai_android_productivity.services.TextExtractionFromImageService
import xyz.haloai.haloai_android_productivity.services.repository.TextExtractionRepository
import xyz.haloai.haloai_android_productivity.services.repository.TextExtractionRepositoryImpl
import xyz.haloai.haloai_android_productivity.ui.viewmodel.TextExtractionFromImageViewModel

val textExtractionFromImageModule = module {
    single { TextExtractionFromImageService() }
    single<TextExtractionRepository>  { TextExtractionRepositoryImpl(get()) }
    viewModel { TextExtractionFromImageViewModel(get()) }
}