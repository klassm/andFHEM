package li.klass.fhem.search

import android.app.SearchManager
import android.content.SearchRecentSuggestionsProvider
import android.database.Cursor
import android.database.MatrixCursor
import android.database.MergeCursor
import android.net.Uri
import li.klass.fhem.AndFHEMApplication
import javax.inject.Inject


class MySearchSuggestionsProvider : SearchRecentSuggestionsProvider() {
    @Inject
    lateinit var searchResultsProvider: SearchResultsProvider

    init {
        setupSuggestions(AUTHORITY, DATABASE_MODE_QUERIES)
    }

    override fun onCreate(): Boolean {
        AndFHEMApplication.getApplication().daggerComponent.inject(this)
        return super.onCreate()
    }

    override fun query(uri: Uri?, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>, sortOrder: String?): Cursor {
        val recent = super.query(uri, projection, selection, selectionArgs, sortOrder)

        val customResultsCursor = queryCache(recent, selectionArgs[0])
        return MergeCursor(arrayOf(recent, customResultsCursor))
    }

    private fun queryCache(recentsCursor: Cursor, userQuery: String): Cursor {
        val arrayCursor = MatrixCursor(recentsCursor.columnNames)

        val formatColumnIndex = recentsCursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_FORMAT)
        val text1Index = recentsCursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1)
        val text2Index = recentsCursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_2)
        val queryColumnIndex = recentsCursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_QUERY)
        val extraDataIndex = recentsCursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA)
        val idIndex = recentsCursor.getColumnIndex("_id")

        val columnCount = recentsCursor.columnCount

        // Populate data here
        var startId = Integer.MAX_VALUE

        val customSearchResults = searchResultsProvider.query(userQuery).allDevices
        for (customSearchResult in customSearchResults) {
            val newRow = arrayOfNulls<Any>(columnCount)
            if (formatColumnIndex >= 0) newRow[formatColumnIndex] = 0
            if (text1Index >= 0) newRow[text1Index] = customSearchResult.aliasOrName
            if (text2Index >= 0) newRow[text2Index] = customSearchResult.roomConcatenated
            if (queryColumnIndex >= 0) newRow[queryColumnIndex] = customSearchResult.name
            if (extraDataIndex >= 0) newRow[extraDataIndex] = customSearchResult.name
            newRow[idIndex] = startId--
            arrayCursor.addRow(newRow)
        }

        return arrayCursor
    }

    companion object {
        val AUTHORITY = MySearchSuggestionsProvider::class.java.name!!
        val MODE = DATABASE_MODE_QUERIES
    }
}