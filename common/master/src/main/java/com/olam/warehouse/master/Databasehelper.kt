package com.olam.warehouse.master

import android.content.Context
import com.olam.warehouse.presentation.utils.Constants
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SQLiteOpenHelper
import java.io.File
import java.io.IOException

/**
 * Created by Baskaran Kannan on 6/7/2021.
 */
class Databasehelper(private val context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {
    private val myDB: SQLiteDatabase? = null
    override fun onCreate(sqLiteDatabase: SQLiteDatabase) {}
    override fun onUpgrade(sqLiteDatabase: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}

    @Throws(IOException::class)
    fun encryptDB() {
        val myPath = DB_PATH + DB_NAME
        val originalFile = context.getDatabasePath(DB_NAME)
        val newFile = File.createTempFile("sqlcipherutils", "tmp", context.getDatabasePath(DB_PATH))

        //As this was for default db (without encryption)!
        val existing_db = SQLiteDatabase.openDatabase(myPath, "", null, SQLiteDatabase.OPEN_READWRITE)

        //And now we are using already encrypted DB!
        //KEY
        //net.sqlcipher.database.SQLiteDatabase existing_db = net.sqlcipher.database.SQLiteDatabase.openDatabase(myPath, KEY, null, net.sqlcipher.database.SQLiteDatabase.OPEN_READWRITE);
        val newPath = newFile.path
        existing_db.rawExecSQL("ATTACH DATABASE '$newPath' AS encrypted KEY '$KEY'")
        existing_db.rawExecSQL("SELECT sqlcipher_export('encrypted')")
        existing_db.rawExecSQL("DETACH DATABASE encrypted")
        existing_db.close()
        //Deleting original plain text db!
        originalFile.delete()
        //Renaming the new db same as old db!
        newFile.renameTo(originalFile)
    }

    @Synchronized
    override fun close() {
        myDB?.close()
        super.close()
    }

    companion object {
        var DB_PATH: String? = null
        var DB_NAME = Constants.VEGADATABASE
        const val DB_VERSION = Constants.ROOM_SCHEMA_VERSION
        const val TB_FTS = "FTS"
        private var instance: Databasehelper? = null
        var KEY: String? = null

        @Synchronized
        fun getInstance(context: Context, Keypar: String?): Databasehelper? {
            if (instance == null) {
                DB_PATH = "/data/data/com.latitude.androidarchitecturecomponent/databases/"
                instance = Databasehelper(context)
                KEY = Keypar
            }
            return instance
        }
    }

}