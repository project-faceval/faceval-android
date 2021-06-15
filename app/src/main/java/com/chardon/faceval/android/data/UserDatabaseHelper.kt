//basepackage com.chardon.faceval.android.data
//
//import android.content.Context
//import android.database.sqlite.SQLiteDatabase
//import android.database.sqlite.SQLiteOpenHelper
//import android.provider.BaseColumns
//import androidx.room.Database
//import com.chardon.faceval.android.data.model.User
//
//const val USER_DB_NAME = "faceval_user.db"
//const val DATABASE_VERSION = 1
//
//object UserContract {
//    object UserEntry : BaseColumns {
//        const val TABLE_NAME = "user"
//        const val COLUMN_NAME_ID = "id"
//        const val COLUMN_NAME_EMAIL = "email"
//        const val COLUMN_NAME_PASSWORD = "password"
//        const val COLUMN_NAME_DATE_JOINED = "date_joined"
//        const val COLUMN_NAME_DISPLAY_NAME = "display_name"
//        const val COLUMN_NAME_GENDER = "gender"
//        const val COLUMN_NAME_STATUS = "status"
//        const val COLUMN_NAME_ACTIVE = "is_active"
//    }
//}
//
//@Database(version = DATABASE_VERSION, entities=[User::class])
//class UserDatabaseHelper(context: Context) : SQLiteOpenHelper(context, USER_DB_NAME, null, DATABASE_VERSION) {
//    override fun onCreate(db: SQLiteDatabase?) {
//        UserContract.UserEntry.apply {
//            db?.execSQL("""CREATE TABLE $TABLE_NAME (
//                $COLUMN_NAME_ID TEXT PRIMARY KEY,
//            )""")
//        }
//    }
//
//    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
//        TODO("Not yet implemented")
//    }
//}