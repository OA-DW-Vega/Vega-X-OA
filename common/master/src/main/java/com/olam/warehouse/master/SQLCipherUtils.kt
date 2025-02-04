package com.olam.warehouse.master

import android.content.Context
import net.sqlcipher.database.SQLiteDatabase
import java.io.File

/**
 * Created by Baskaran Kannan on 6/7/2021.
 */

object SQLCipherUtils {
    /**
     * Determine whether or not this database appears to be encrypted, based
     * on whether we can open it without a passphrase.
     *
     * @param ctxt a Context
     * @param dbName the name of the database, as used with Room, SQLiteOpenHelper,
     * etc.
     * @return the detected state of the database
     */
    fun getDatabaseState(ctxt: Context, dbName: String?): State {
        SQLiteDatabase.loadLibs(ctxt)
        return getDatabaseState(ctxt.getDatabasePath(dbName))
    }

    /**
     * Determine whether or not this database appears to be encrypted, based
     * on whether we can open it without a passphrase.
     *
     * NOTE: You are responsible for ensuring that net.sqlcipher.database.SQLiteDatabase.loadLibs()
     * is called before calling this method. This is handled automatically with the
     * getDatabaseState() method that takes a Context as a parameter.
     *
     * @param dbPath a File pointing to the database
     * @return the detected state of the database
     */
    fun getDatabaseState(dbPath: File): State {
        if (dbPath.exists()) {
            var db: SQLiteDatabase? = null
            return try {
                db = SQLiteDatabase.openDatabase(dbPath.absolutePath, "",
                        null, SQLiteDatabase.OPEN_READONLY)
                db.version
                State.UNENCRYPTED
            } catch (e: Exception) {
                State.ENCRYPTED
            } finally {
                db?.close()
            }
        }
        return State.DOES_NOT_EXIST
    }

    enum class State {
        DOES_NOT_EXIST, UNENCRYPTED, ENCRYPTED
    }
}