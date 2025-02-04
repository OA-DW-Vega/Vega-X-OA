
package com.olam.warehouse.master

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.olam.warehouse.ginning.ui.pile.db.dao.GinningPileDao
import com.olam.warehouse.master.migration.COTT_MIGRATION_1_2
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.dao.GinningInprogressDao
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.dao.ReceivingDao
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.dao.VegaCottonGinningDispatchDao
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.dao.VegaGinningIncomingLotDao
import com.olam.warehouse.master.vegaCotton.vegaGinningWareHouse.entity.*
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.dao.DispatchDao
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.dao.MtnDispatchDao
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.dao.PortReceivingDao
import com.olam.warehouse.master.vegaCotton.vegaPortWareHouse.entity.*
import com.olam.warehouse.portwarehouse.ui.pile.db.dao.PortPileDao
import com.olam.warehouse.presentation.converters.Converters
import com.olam.warehouse.presentation.converters.EnumTypeConverter
import com.olam.warehouse.presentation.utils.Constants
import com.olam.warehouse.presentation.utils.Constants.ROOM_SCHEMA_VERSION_COTTON
import com.olam.warehouse.presentation.utils.PreferenceHelper
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory

/**
 * Created by Pavani Buchupalli on 03/03/2021.
 */
@Database(
    entities = [
        VegaCottonGinningDispatchDelivery::class,
        Bale::class,
        Grade::class,
        PileBale::class,
        Mtn::class,
        MtnGrades::class,
        MtnBales::class,
        PortPileBale::class,
        MtnDispatchDelivery::class,
        Container::class,
        DispatchOT::class,
        PortMtn::class,
        MtnBale::class,
        PortBale::class,
        PortMtnGrades::class,
        PortMtnBales::class,
        MtnGrade::class
    ], version = ROOM_SCHEMA_VERSION_COTTON, exportSchema = false
)
@TypeConverters(Converters::class, EnumTypeConverter::class)
abstract class CottonDataBase : RoomDatabase() {
    abstract fun vegaCottonGinningDispatchDao(): VegaCottonGinningDispatchDao
    abstract fun vegaGinningIncomingLotDao(): VegaGinningIncomingLotDao
    abstract fun vegaGinningPileDao(): GinningPileDao
    abstract fun vegaGinningInprogressDao(): GinningInprogressDao
    abstract fun vegaGinningReceivingDao(): ReceivingDao
    abstract fun portPileDao(): PortPileDao
    abstract fun portMtnDispatchDao(): MtnDispatchDao
    abstract fun portDispatchDao(): DispatchDao
    abstract fun portReceivingDao(): PortReceivingDao

    companion object {
        lateinit var COTTON_DB_INSTANCE: CottonDataBase
        fun buildDatabase(context: Context): CottonDataBase {
            PreferenceHelper.save(Constants.DBVEGA, Constants.DVBGA)
            val admin = PreferenceHelper.get(Constants.DBVEGA, "").toCharArray()
//            val dbKey = DatabaseKeyMgr.getInstance().getCharKey(admin, context)
            val supportFactory = SupportFactory(SQLiteDatabase.getBytes(admin))
            COTTON_DB_INSTANCE =
                Room.databaseBuilder(
                    context.applicationContext,
                    CottonDataBase::class.java,
                    Constants.COTTONDATABASE
                )
                    .addMigrations(COTT_MIGRATION_1_2)
                    //.fallbackToDestructiveMigration()
                    .openHelperFactory(supportFactory)
                    .build()
            return COTTON_DB_INSTANCE
        }

        fun getInstance(): CottonDataBase? {
            return COTTON_DB_INSTANCE
        }
    }
}
