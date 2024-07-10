package xyz.haloai.haloai_android_productivity.di

import android.content.Context
import androidx.room.Room
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import xyz.haloai.haloai_android_productivity.data.local.ProductivityFeedDatabase
import xyz.haloai.haloai_android_productivity.data.local.repository.ProductivityFeedRepository
import xyz.haloai.haloai_android_productivity.ui.viewmodel.ProductivityFeedViewModel

val productivityFeedModule = module {
    single {
        Room.databaseBuilder(
            get<Context>(),
            ProductivityFeedDatabase::class.java,
            "productivityFeed"
        ).build()
    }

    single { get<ProductivityFeedDatabase>().productivityFeedDao() }
    single { ProductivityFeedRepository(get()) }

    viewModel { ProductivityFeedViewModel(get()) }
}
