package com.chardon.faceval.android.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.chardon.faceval.android.data.dao.UserDao
import com.chardon.faceval.android.data.model.User

@Database(version = 1, entities = [User::class])
abstract class UserDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}