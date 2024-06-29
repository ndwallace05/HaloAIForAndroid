package xyz.haloai.haloai_android_productivity.di

import android.content.Context
import androidx.room.Room
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import xyz.haloai.haloai_android_productivity.data.local.ScheduleEntriesDatabase
import xyz.haloai.haloai_android_productivity.data.local.repository.ScheduleDbRepository
import xyz.haloai.haloai_android_productivity.ui.viewmodel.ScheduleDbViewModel

val scheduleDbModule = module {
    single {
        Room.databaseBuilder(
            get<Context>(),
            ScheduleEntriesDatabase::class.java,
            "scheduleDb"
        ).build()
    }

    single { get<ScheduleEntriesDatabase>().scheduleEntriesDao() }
    single { ScheduleDbRepository(get()) }

    viewModel { (context: Context, refreshCalendar: Boolean) -> ScheduleDbViewModel(get(), context, refreshCalendar) }
}