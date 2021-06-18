package com.chardon.faceval.android.data

import android.content.Context
import android.service.autofill.UserData
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.chardon.faceval.android.data.dao.UserDao
import com.chardon.faceval.android.data.model.User

@Database(version = 1, entities = [User::class], exportSchema = false)
abstract class UserDatabase : RoomDatabase() {
    abstract val userDao: UserDao
    
    companion object {

        @Volatile
        private var INSTANCE: UserDatabase? = null

        fun getInstance(context: Context): UserDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        UserDatabase::class.java,
                        "user",
                    ).fallbackToDestructiveMigration().build()

                    INSTANCE = instance
                }

                return instance
            }
        }
    }
}