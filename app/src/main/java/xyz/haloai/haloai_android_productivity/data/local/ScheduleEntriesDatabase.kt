package xyz.haloai.haloai_android_productivity.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import xyz.haloai.haloai_android_productivity.data.local.dao.ScheduleEntriesDao
import xyz.haloai.haloai_android_productivity.data.local.entities.ScheduleEntry
import xyz.haloai.haloai_android_productivity.data.local.typeConverters.DateConverter
import xyz.haloai.haloai_android_productivity.data.local.typeConverters.StringListConverter
import xyz.haloai.haloai_android_productivity.data.local.typeConverters.eventTypeConverter
import xyz.haloai.haloai_android_productivity.data.local.typeConverters.timeSlotConverter

@Database(entities = [ScheduleEntry::class], version = 3)
@TypeConverters(DateConverter::class, StringListConverter::class, timeSlotConverter::class, eventTypeConverter::class)
public abstract class ScheduleEntriesDatabase : RoomDatabase() {
    abstract fun scheduleEntriesDao(): ScheduleEntriesDao

    companion object {
        @Volatile
        private var INSTANCE: ScheduleEntriesDatabase? = null

        fun getDatabase(context: Context): ScheduleEntriesDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ScheduleEntriesDatabase::class.java,
                    "calendarEventsDb"
                ).build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}