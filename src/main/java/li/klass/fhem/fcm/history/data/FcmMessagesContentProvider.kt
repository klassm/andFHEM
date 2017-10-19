package li.klass.fhem.fcm.history.data

import android.content.*
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteQueryBuilder
import android.net.Uri

class FcmMessagesContentProvider : ContentProvider() {
    private lateinit var openHelper: FcmHistoryDatabaseOpenHelper

    override fun onCreate(): Boolean {
        openHelper = FcmHistoryDatabaseOpenHelper(context)
        return true
    }

    override fun insert(uri: Uri?, values: ContentValues?): Uri {
        val database = openHelper.writableDatabase

        val id = database.insertWithOnConflict(tableName, null, values, SQLiteDatabase.CONFLICT_REPLACE)
        return ContentUris.withAppendedId(uri, id)
    }

    override fun query(uri: Uri?, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor {
        val db = openHelper.readableDatabase

        val builder = SQLiteQueryBuilder().apply {
            tables = tableName
        }

        return builder.query(db, projection, selection, selectionArgs, null, null, sortOrder ?: "$columnDatetime DESC")
    }

    override fun update(uri: Uri?, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int {
        throw UnsupportedOperationException()
    }

    override fun delete(uri: Uri?, selection: String?, selectionArgs: Array<out String>?): Int {
        return openHelper.writableDatabase
                .delete(tableName, selection, selectionArgs)
    }

    override fun getType(uri: Uri?): String? =
            if (uriMatcher.match(uri) == fcmItemMatchCode) "fcm_history_item" else null

    private class FcmHistoryDatabaseOpenHelper(context: Context?) : SQLiteOpenHelper(context, tableName, null, 5) {
        override fun onCreate(db: SQLiteDatabase?) {
            db?.execSQL(createTableStatement)
        }

        override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        }
    }

    companion object {
        private val tableName = "fcm_history_messages"
        private val columnId = "ID"
        val columnDatetime = "DATETIME"
        val columnDate = "DATE"
        val columnTitle: String = "TITLE"
        val columnText: String = "TEXT"
        val columnTicker: String = "TICKER"

        private val authority = "li.klass.fhem.fcm.history.messages"
        val contentUri = Uri.parse("content://$authority/$tableName")!!

        private val fcmItemMatchCode = 1
        private val uriMatcher = createUriMatcher()

        private val createTableStatement = """CREATE TABLE
                |$tableName
                |(
                    |$columnId INTEGER PRIMARY KEY,
                    |$columnDatetime TEXT,
                    |$columnDate TEXT,
                    |$columnTitle TEXT,
                    |$columnText TEXT,
                    |$columnTicker TEXT
                |)
                |""".trimMargin()

        private fun createUriMatcher(): UriMatcher {
            val matcher = UriMatcher(UriMatcher.NO_MATCH)
            matcher.addURI(authority, "message", fcmItemMatchCode)
            return matcher
        }
    }
}