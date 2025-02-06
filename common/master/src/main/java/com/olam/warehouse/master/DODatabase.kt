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
import com.olam.warehouse.master.migration.*
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
        DOBinDetails::class,
        DOSapMaterialList::class
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
            val dbState = SQLCipherUtils.getDatabaseState(context.applicationContext, Constants.DODATABASE)

            when (dbState) {
                SQLCipherUtils.State.UNENCRYPTED -> {
                    when (PreferenceHelper.get(Constants.FIRST_TIME_OPENED_OD, 0)) {
                        0 -> {
                            //VEGA_DB_INSTANCE = getNoEncryptDB(context)
                            DO_DB_INSTANCE = getEncryptDB(context)
                            PreferenceHelper.save(
                                Constants.FIRST_TIME_OPENED_OD,
                                PreferenceHelper.get(Constants.FIRST_TIME_OPENED_OD, 0) + 1
                            )
                        }
                        1 -> {
//                            encryptDB(dbState, context)
                            DO_DB_INSTANCE = getEncryptDB(context)
                            PreferenceHelper.save(
                                Constants.FIRST_TIME_OPENED_OD,
                                PreferenceHelper.get(Constants.FIRST_TIME_OPENED_OD, 0) + 1
                            )
                        }
                    }
                }
                else -> {
                    DO_DB_INSTANCE = getEncryptDB(context)
                }
            }

            return DO_DB_INSTANCE


        }

        fun getInstance(): DODatabase? {
            return DO_DB_INSTANCE
        }


        private fun getEncryptDB(context: Context): DODatabase {
            val admin = PreferenceHelper.get(Constants.DBVEGA, "").toCharArray()
            val supportFactory = SupportFactory(SQLiteDatabase.getBytes(admin))
            return Room.databaseBuilder(
                context.applicationContext,
                DODatabase::class.java,
                Constants.DODATABASE
            )
                .addMigrations(
                    ODMIGRATION_12_13, ODMIGRATION_13_14, ODMIGRATION_14_15
                )
//                .fallbackToDestructiveMigration()
                .openHelperFactory(supportFactory)
                .build()
        }

    }
}



