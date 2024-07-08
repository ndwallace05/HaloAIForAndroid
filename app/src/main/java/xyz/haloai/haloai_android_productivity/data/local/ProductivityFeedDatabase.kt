package xyz.haloai.haloai_android_productivity.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import xyz.haloai.haloai_android_productivity.data.local.dao.ProductivityFeedDao
import xyz.haloai.haloai_android_productivity.data.local.entities.FeedCard
import xyz.haloai.haloai_android_productivity.data.local.typeConverters.DateConverter
import xyz.haloai.haloai_android_productivity.data.local.typeConverters.feedCardTypeConverter
import xyz.haloai.haloai_android_productivity.data.local.typeConverters.importanceScoreConverter

@Database(entities = [FeedCard::class], version = 1)
@TypeConverters(DateConverter::class, feedCardTypeConverter::class, importanceScoreConverter::class)
public abstract class ProductivityFeedDatabase : RoomDatabase() {
    abstract fun productivityFeedDao(): ProductivityFeedDao

    companion object {
        @Volatile
        private var INSTANCE: ProductivityFeedDatabase? = null

        fun getDatabase(context: Context): ProductivityFeedDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ProductivityFeedDatabase::class.java,
                    "productivityFeed"
                ).build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}