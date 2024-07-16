package xyz.haloai.haloai_android_productivity.di

import org.koin.dsl.module
import xyz.haloai.haloai_android_productivity.workers.workerFactory.CalendarUpdateWorkerFactory
import xyz.haloai.haloai_android_productivity.workers.workerFactory.CombinedWorkerFactory
import xyz.haloai.haloai_android_productivity.workers.workerFactory.EmailCheckWorkerFactory
import xyz.haloai.haloai_android_productivity.workers.workerFactory.SuggestedTasksWorkerFactory

val workManagerModule = module {
    single { EmailCheckWorkerFactory() }
    single { CalendarUpdateWorkerFactory() }
    single { SuggestedTasksWorkerFactory() }
    single { CombinedWorkerFactory(get(), get(), get()) }
}