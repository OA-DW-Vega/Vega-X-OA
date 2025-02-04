package com.olam.warehouse.master

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.olam.warehouse.master.common.dao.MasterDao
import com.olam.warehouse.master.dorigin.dao.DOQualityDao
import com.olam.warehouse.master.dorigin.dao.DOReceivingDao
import com.olam.warehouse.master.dorigin.entity.*
import com.olam.warehouse.presentation.converters.Converters
import com.olam.warehouse.presentation.converters.EnumTypeConverter
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.Constants.ROOM_SCHEMA_VERSION_OD
import com.olam.warehouse.presentation.utils.PreferenceHelper
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory

/**
 * Created by Baskaran Kannan on 12/26/2019.
 */
@Database(
    entities = [DOReceiving::class,
        DOReceivingLineItem::class,
        DOMaterial::class,
        DOPackageMaterial::class,
        DOQualitative::class,
        DOQuality::class,
        DOQualityParameter::class,
        DOQualityWBDetails::class,
        DOReceivingMtn::class,
        DOReceivingMtnLots::class,
        DOReceivingWarehouse::class,
        DOStorageLocation::class,
        DOVendor::class,
        DOWarehouse::class,
        DOTransactionDetail::class,
        DOBag::class,
        DispatchDetail::class,
        DOCustomStLocation::class,
        DOBinDetails::class
    ], version = ROOM_SCHEMA_VERSION_OD, exportSchema = false
)
@TypeConverters(Converters::class, EnumTypeConverter::class)
abstract class DODatabase : RoomDatabase() {
    abstract fun doQualityDao(): DOQualityDao
    abstract fun doReceivingDao(): DOReceivingDao
    abstract fun doMasterDao(): MasterDao

    companion object {
        lateinit var DO_DB_INSTANCE: DODatabase
        fun buildDatabase(context: Context): DODatabase {
            PreferenceHelper.save(Constants.DBVEGA, Constants.DVBGAOD)
            val admin = PreferenceHelper.get(Constants.DBVEGA, "").toCharArray()
//            val dbKey = DatabaseKeyMgr.getInstance().getCharKey(admin, context)
            val supportFactory = SupportFactory(SQLiteDatabase.getBytes(admin))
            DO_DB_INSTANCE =
                Room.databaseBuilder(
                    context.applicationContext,
                    DODatabase::class.java,
                    Constants.DODATABASE
                ).openHelperFactory(supportFactory)
                    .build()
            return DO_DB_INSTANCE
        }

        fun getInstance(): DODatabase? {
            return DO_DB_INSTANCE
        }
    }
}
