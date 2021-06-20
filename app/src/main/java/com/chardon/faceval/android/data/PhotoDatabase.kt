//package com.chardon.faceval.android.data
//
//import android.content.Context
//import androidx.room.Database
//import androidx.room.Room
//import androidx.room.RoomDatabase
//import com.chardon.faceval.android.data.dao.PhotoDao
//import com.chardon.faceval.android.data.model.Photo
//
//@Database(version = 1, entities = [Photo::class])
//abstract class PhotoDatabase : RoomDatabase() {
//    abstract fun photoDao(): PhotoDao
//
//    companion object {
//
//        @Volatile
//        private var INSTANCE: PhotoDatabase? = null
//
//        fun getInstance(context: Context): PhotoDatabase {
//            synchronized(this) {
//                var instance = INSTANCE
//
//                if (instance == null) {
//                    instance = Room.databaseBuilder(
//                        context.applicationContext,
//                        PhotoDatabase::class.java,
//                        "user",
//                    ).fallbackToDestructiveMigration().build()
//
//                    INSTANCE = instance
//                }
//
//                return instance
//            }
//        }
//    }
//}