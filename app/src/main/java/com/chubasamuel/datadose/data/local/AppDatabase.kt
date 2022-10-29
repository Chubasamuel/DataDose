package com.chubasamuel.datadose.data.local

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities=[Projects::class,ProjectDetail::class,ProjectFilled::class],version=2, exportSchema = true,
autoMigrations = [AutoMigration(from=1,to=2)]
    )
@TypeConverters(Converters::class)
abstract class AppDatabase :RoomDatabase(){
    abstract fun getAppDao():AppDao

    companion object{
        @Volatile private var instance:AppDatabase?=null
        fun getInstance(ctx:Context):AppDatabase=
            instance?: synchronized(this){instance?:buildDatabase(ctx).also{instance=it}}
        private fun buildDatabase(appContext: Context)=
            Room.databaseBuilder(appContext,AppDatabase::class.java,"dcor_data_dose_db")
                .build()
    }
}