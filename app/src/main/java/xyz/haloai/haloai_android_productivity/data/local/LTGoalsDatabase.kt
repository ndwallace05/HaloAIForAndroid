package xyz.haloai.haloai_android_productivity.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import xyz.haloai.haloai_android_productivity.data.local.dao.LTGoalDao
import xyz.haloai.haloai_android_productivity.data.local.entities.LTGoal
import xyz.haloai.haloai_android_productivity.data.local.typeConverters.DateConverter

@Database(entities = [LTGoal::class], version = 1)
@TypeConverters(DateConverter::class)
public abstract class LTGoalsDatabase : RoomDatabase() {
    abstract fun ltGoalDao(): LTGoalDao

    companion object {
        @Volatile
        private var INSTANCE: LTGoalsDatabase? = null

        fun getDatabase(context: Context): LTGoalsDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LTGoalsDatabase::class.java,
                    "ltGoalsDb"
                ).build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}