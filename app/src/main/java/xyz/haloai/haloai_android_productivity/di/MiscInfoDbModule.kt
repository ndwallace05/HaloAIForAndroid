package xyz.haloai.haloai_android_productivity.di

import android.content.Context
import androidx.room.Room
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import xyz.haloai.haloai_android_productivity.data.local.MiscInfoDatabase
import xyz.haloai.haloai_android_productivity.data.local.repository.MiscInfoDbRepository
import xyz.haloai.haloai_android_productivity.ui.viewmodel.MiscInfoDbViewModel

val miscInfoDbModule = module {
    single {
        Room.databaseBuilder(
            get<Context>(),
            MiscInfoDatabase::class.java,
            "miscInfoDb"
        ).build()
    }

    single { get<MiscInfoDatabase>().miscInfoDao() }
    single { MiscInfoDbRepository(get()) }

    viewModel { MiscInfoDbViewModel(get()) }
}