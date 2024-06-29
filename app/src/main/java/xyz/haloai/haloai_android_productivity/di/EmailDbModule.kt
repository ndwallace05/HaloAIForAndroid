package xyz.haloai.haloai_android_productivity.di

import android.content.Context
import androidx.room.Room
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import xyz.haloai.haloai_android_productivity.data.local.EmailDatabase
import xyz.haloai.haloai_android_productivity.data.local.repository.EmailDbRepository
import xyz.haloai.haloai_android_productivity.data.ui.viewmodel.EmailDbViewModel

val emailDbModule = module {
    single {
        Room.databaseBuilder(
            get<Context>(),
            EmailDatabase::class.java,
            "emailDb"
        ).build()
    }

    single { get<EmailDatabase>().emailAccountDao() }
    single { EmailDbRepository(get()) }

    viewModel { EmailDbViewModel(get()) }
}