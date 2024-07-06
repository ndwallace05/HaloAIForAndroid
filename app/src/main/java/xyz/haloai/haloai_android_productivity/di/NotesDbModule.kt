package xyz.haloai.haloai_android_productivity.di

import android.content.Context
import androidx.room.Room
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import xyz.haloai.haloai_android_productivity.data.local.NotesDatabase
import xyz.haloai.haloai_android_productivity.data.local.repository.NotesDbRepository
import xyz.haloai.haloai_android_productivity.ui.viewmodel.NotesDbViewModel

val notesDbModule = module {
    single {
        Room.databaseBuilder(
            get<Context>(),
            NotesDatabase::class.java,
            "notesDb"
        ).build()
    }

    single { get<NotesDatabase>().noteDao() }
    single { NotesDbRepository(get()) }

    viewModel { NotesDbViewModel(get()) }
}