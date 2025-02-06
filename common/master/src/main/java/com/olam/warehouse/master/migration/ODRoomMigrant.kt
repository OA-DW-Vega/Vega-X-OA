package com.olam.warehouse.master.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Created by Baskaran Kannan on 8/10/2022.
 */


val ODMIGRATION_12_13: Migration = object : Migration(12, 13) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE DOSapMaterialList (tradingProductId TEXT NOT NULL,tradingProductName TEXT,sapMaterialId TEXT, PRIMARY KEY(tradingProductId))")
    }
}

val ODMIGRATION_13_14: Migration = object : Migration(13, 14) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE DOTransactionDetail ADD COLUMN sourceLotId ''")
        database.execSQL("ALTER TABLE DOTransactionDetail ADD COLUMN eudrFlag INTEGER DEFAULT 0 NOT NULL")
        database.execSQL("ALTER TABLE DOQualityWBDetails ADD COLUMN sourceLotId ''")
        database.execSQL("ALTER TABLE DOQualityWBDetails ADD COLUMN eudrFlag INTEGER DEFAULT 0 NOT NULL")
    }
}

val ODMIGRATION_14_15: Migration = object : Migration(14, 15) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE DOMaterial ADD COLUMN complainceFlag ''")
        database.execSQL("ALTER TABLE DOQualityWBDetails ADD COLUMN eudrComplaint ''")
        database.execSQL("ALTER TABLE DOQualityWBDetails ADD COLUMN sourceLotId ''")
        database.execSQL("ALTER TABLE DOTransactionDetail ADD COLUMN eudrComplaint ''")
        database.execSQL("ALTER TABLE DOTransactionDetail ADD COLUMN sourceLotId ''")
    }
}
