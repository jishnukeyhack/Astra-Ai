package com.example.aisecretary.data.local.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.aisecretary.data.local.database.dao.MessageDao
import com.example.aisecretary.data.local.database.dao.MemoryFactDao
import com.example.aisecretary.data.model.Message
import com.example.aisecretary.data.model.MemoryFact
import android.content.Context

@Database(entities = [Message::class, MemoryFact::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
    abstract fun memoryFactDao(): MemoryFactDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Migration from version 1 to 2
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Recreate memory_facts table with the index
                // First, create a backup of existing data
                database.execSQL("CREATE TABLE IF NOT EXISTS `memory_facts_backup` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `key` TEXT NOT NULL, `value` TEXT NOT NULL, `timestamp` INTEGER NOT NULL)")
                database.execSQL("INSERT INTO `memory_facts_backup` SELECT * FROM `memory_facts`")
                
                // Drop the old table
                database.execSQL("DROP TABLE IF EXISTS `memory_facts`")
                
                // Create the new table with the index
                database.execSQL("CREATE TABLE IF NOT EXISTS `memory_facts` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `key` TEXT NOT NULL, `value` TEXT NOT NULL, `timestamp` INTEGER NOT NULL)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_memory_facts_key` ON `memory_facts` (`key`)")
                
                // Restore the data
                database.execSQL("INSERT INTO `memory_facts` SELECT * FROM `memory_facts_backup`")
                
                // Drop the backup table
                database.execSQL("DROP TABLE IF EXISTS `memory_facts_backup`")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                .addMigrations(MIGRATION_1_2)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}