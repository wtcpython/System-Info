package com.wtc.systeminfo

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [SocEntity::class], version = 1, exportSchema = false)
abstract class SocDatabase : RoomDatabase() {
    abstract fun socDao(): SocDao

    companion object {
        @Volatile
        private var INSTANCE: SocDatabase? = null

        fun getInstance(context: Context): SocDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SocDatabase::class.java,
                    "soc.db"
                )
                    .createFromAsset("soc.db")  // 从 assets 目录加载预置数据库
                    .fallbackToDestructiveMigration(false) // 根据需求加不加
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
