//package com.chardon.faceval.android.data.dao
//
//import androidx.room.*
//import com.chardon.faceval.android.data.model.Photo
//import java.util.*
//
//@Dao
//interface PhotoDao {
//
//    @Query("SELECT * FROM photo;")
//    fun getAll(): List<Photo>
//
////    @Query("SELECT * FROM photo WHERE user_id = :userId;")
////    fun getByUser(userId: String): List<Photo>
//
//    @Query("SELECT * FROM photo WHERE uuid_id = :uuid;")
//    fun getByUUID(uuid: String): Photo
//
//    @Query("SELECT * FROM photo LIMIT 1;")
//    fun getCurrent(): Photo?
//
//    @Insert
//    fun insert(photo: Photo)
//
//    @Update
//    fun update(photo: Photo)
//
//    @Delete
//    fun delete(photo: Photo)
//
//    @Query("DELETE FROM photo;")
//    fun clear()
//}