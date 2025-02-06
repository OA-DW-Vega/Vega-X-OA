package com.olam.warehouse.master.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Created by Baskaran Kannan on 8/4/2020.
 */

val COTT_MIGRATION_1_2: Migration = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        //database.execSQL("ALTER TABLE Container ADD COLUMN isSelected INTEGER ")
        database.execSQL("ALTER TABLE DispatchOT ADD COLUMN splitOt TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE DispatchOT ADD COLUMN originalOt TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE DispatchOT ADD COLUMN originalDelQty TEXT DEFAULT ''")
        database.execSQL("ALTER TABLE DispatchOT ADD COLUMN originalNoOfCont INTEGER ")
    }
}

