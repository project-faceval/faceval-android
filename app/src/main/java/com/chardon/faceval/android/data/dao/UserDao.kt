package com.chardon.faceval.android.data.dao

import androidx.room.*
import com.chardon.faceval.android.data.model.User
import com.chardon.faceval.android.data.model.UserWithPhotoList

@Dao
interface UserDao {

    @Query("SELECT * FROM user;")
    fun getAll(): List<User>

    @Query("SELECT * FROM user WHERE is_active = 1 LIMIT 1;")
    fun getCurrent(): User

    @Transaction
    @Query("SELECT * FROM user WHERE is_active = 1 LIMIT 1")
    fun getUserDetails(): UserWithPhotoList

    @Insert
    fun insert(newUser: User): Int

    @Delete
    fun delete(user: User): Int

    @Update
    fun update(user: User): Int
}