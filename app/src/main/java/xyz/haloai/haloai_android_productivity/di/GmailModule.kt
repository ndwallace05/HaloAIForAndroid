package xyz.haloai.haloai_android_productivity.di

import android.content.Context
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import xyz.haloai.haloai_android_productivity.services.GmailService
import xyz.haloai.haloai_android_productivity.services.repository.GmailRepository
import xyz.haloai.haloai_android_productivity.services.repository.GmailRepositoryImpl
import xyz.haloai.haloai_android_productivity.ui.viewmodel.GmailViewModel

val gmailModule = module {
    single { GmailService(androidContext()) }
    single<GmailRepository>  { GmailRepositoryImpl(get()) }
    viewModel { GmailViewModel(get()) }
}