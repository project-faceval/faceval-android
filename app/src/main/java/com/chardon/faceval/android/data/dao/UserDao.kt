package com.chardon.faceval.android.data.dao

import androidx.room.*
import com.chardon.faceval.android.data.model.User

@Dao
interface UserDao {

    @Query("SELECT * FROM user;")
    fun getAll(): List<User>

    @Query("SELECT * FROM user WHERE is_active = 1 LIMIT 1;")
    fun getCurrent(): User

//    @Transaction
//    @Query("SELECT * FROM user WHERE is_active = 1 LIMIT 1")
//    fun getUserDetails(): UserWithPhotoList

    @Insert
    fun insert(vararg users: User)

    @Delete
    fun delete(user: User)

    @Query("DELETE FROM user;")
    fun deleteAll()

    @Update
    fun update(user: User)
}