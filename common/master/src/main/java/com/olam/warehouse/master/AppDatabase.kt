package com.olam.warehouse.master

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.olam.warehouse.master.common.utils.getCurrentKey
import com.olam.warehouse.master.migration.MIGRATION_1_2
import com.olam.warehouse.master.migration.MIGRATION_2_3_App
import com.olam.warehouse.master.user.dao.UserDao
import com.olam.warehouse.master.user.entity.User
import com.olam.warehouse.master.user.entity.UserRole
import com.olam.warehouse.presentation.converters.Converters
import com.olam.warehouse.presentation.converters.EnumTypeConverter
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.Constants.ROOM_SCHEMA_VERSION_APP_DATABASE
import com.olam.warehouse.presentation.utils.PreferenceHelper
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory

@Database(
    entities = [User::class,
        UserRole::class
    ], version = ROOM_SCHEMA_VERSION_APP_DATABASE, exportSchema = false
)
@TypeConverters(Converters::class, EnumTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao

    companion object {
        lateinit var APP_DB_INSTANCE: AppDatabase
        fun buildDatabase(context: Context): AppDatabase {
            if (getCurrentKey().contains("DO"))
                PreferenceHelper.save(Constants.DBVEGA, Constants.DVBGAOD)
            else
                PreferenceHelper.save(Constants.DBVEGA, Constants.DVBGA)
            val admin = PreferenceHelper.get(Constants.DBVEGA, "").toCharArray()
//            val dbKey = DatabaseKeyMgr.getInstance().getCharKey(admin, context)
            val supportFactory = SupportFactory(SQLiteDatabase.getBytes(admin))
            APP_DB_INSTANCE =
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    Constants.DATABASE
                )
                    .addMigrations(
                        MIGRATION_1_2, MIGRATION_2_3_App
                    ).openHelperFactory(supportFactory).build()
            return APP_DB_INSTANCE
        }

        fun getInstance(): AppDatabase? {
            return APP_DB_INSTANCE
        }
    }
}
