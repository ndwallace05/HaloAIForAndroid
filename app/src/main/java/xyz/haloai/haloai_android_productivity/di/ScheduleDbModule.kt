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

    // viewModel { (context: Context, refreshDb: Boolean) -> ScheduleDbViewModel(get(), context, refreshDb) }

    viewModel { params ->
        ScheduleDbViewModel(
            repository = get(),
            context = params.get(),
            refreshDb = params.getOrNull() ?: true
        )
    }

    /*viewModel { (parameters: ParametersDefinition) ->
        para
        val context = parameters.get<Context>()
        val booleanParam = parameters.getOrNull<Boolean>() ?: true
        ScheduleDbViewModel(get(), context, booleanParam)
    }*/
}