package xyz.haloai.haloai_android_productivity.di

import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import xyz.haloai.haloai_android_productivity.services.ProductivityFeedOptionsFunctions
import xyz.haloai.haloai_android_productivity.services.repository.ProductivityFeedOptionsImpl
import xyz.haloai.haloai_android_productivity.services.repository.ProductivityFeedOptionsRepository
import xyz.haloai.haloai_android_productivity.ui.viewmodel.ProductivityFeedOptionsViewModel

val productivityFeedOptionsModule = module {
    single { ProductivityFeedOptionsFunctions(androidContext()) }
    single<ProductivityFeedOptionsRepository>  { ProductivityFeedOptionsImpl(get()) }
    viewModel { ProductivityFeedOptionsViewModel(get()) }
}