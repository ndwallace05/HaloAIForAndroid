package xyz.haloai.haloai_android_productivity.di

import android.content.Context
import androidx.room.Room
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import xyz.haloai.haloai_android_productivity.data.local.LTGoalsDatabase
import xyz.haloai.haloai_android_productivity.data.local.repository.LTGoalsRepository
import xyz.haloai.haloai_android_productivity.ui.viewmodel.LTGoalsViewModel

val ltGoalsDbModule = module {
    single {
        Room.databaseBuilder(
            get<Context>(),
            LTGoalsDatabase::class.java,
            "ltGoalsDb"
        ).build()
    }

    single { get<LTGoalsDatabase>().ltGoalDao() }
    single { LTGoalsRepository(get()) }

    viewModel { LTGoalsViewModel(get()) }
}