package com.example.battleship.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.battleship.database.model.BattleshipMatchResult

class DatabaseHelper(
    context: Context,
    factory: SQLiteDatabase.CursorFactory?
) : SQLiteOpenHelper(
    context, DATABASE_NAME,
    factory, DATABASE_VERSION
) {
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS" + TABLE_NAME)
        onCreate(db)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE_TABLE" + TABLE_NAME + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_OPPONENT_NAME + " TEXT,"
                    + COLUMN_RESULT + " TEXT,"
                    + COLUMN_DURATION + " INTEGER,"
                    + COLUMN_OPPONENT_FIELD + " TEXT,"
                    + COLUMN_PLAYER_FIELD + " TEXT" + ")"
        )
    }

    fun insertMatchResult(
        result: Int, playerField: String,
        opponentField: String,
        duration: Int,
        opponentName: String
    ): Long {
        val db = this.writableDatabase
        val values = ContentValues()

        values.put(COLUMN_DURATION, duration)
        values.put(COLUMN_OPPONENT_FIELD, opponentField)
        values.put(COLUMN_PLAYER_FIELD, playerField)
        values.put(COLUMN_OPPONENT_NAME, opponentName)
        values.put(COLUMN_RESULT, result)

        val id: Long = db.insert(TABLE_NAME, null, values)
        db.close()
        return id
    }

    fun getMatchResult(id: Long): BattleshipMatchResult {
        val db = this.readableDatabase

        val cursor = db.query(
            TABLE_NAME,
            arrayOf(
                COLUMN_ID,
                COLUMN_DURATION,
                COLUMN_OPPONENT_FIELD,
                COLUMN_PLAYER_FIELD,
                COLUMN_OPPONENT_NAME,
                COLUMN_RESULT
            ),
            "$COLUMN_ID=?", arrayOf(id.toString()), null, null, null, null
        )

        if (cursor != null) {
            cursor.moveToFirst()
        }

        val matchResult = BattleshipMatchResult(
            cursor.getInt(cursor.getColumnIndex(COLUMN_ID)),
            cursor.getInt(cursor.getColumnIndex(COLUMN_RESULT)),
            cursor.getString(
                cursor.getColumnIndex(
                    COLUMN_PLAYER_FIELD
                )
            ),
            cursor.getString(cursor.getColumnIndex(COLUMN_OPPONENT_FIELD)),
            cursor.getInt(
                cursor.getColumnIndex(
                    COLUMN_DURATION
                )
            ),
            cursor.getString(cursor.getColumnIndex(COLUMN_OPPONENT_NAME))
        )

        cursor.close()
        db.close()

        return matchResult
    }

    fun getAllMatchResults(): List<BattleshipMatchResult> {
        val matchResults: MutableList<BattleshipMatchResult> = ArrayList()

        val selectQuery = "SELECT  * FROM $TABLE_NAME ORDER BY $COLUMN_ID DESC"

        val db = this.writableDatabase
        val cursor = db.rawQuery(selectQuery, null)

        if (cursor.moveToFirst()) {
            do {
                val matchResult = BattleshipMatchResult(
                    cursor.getInt(cursor.getColumnIndex(COLUMN_ID)),
                    cursor.getInt(cursor.getColumnIndex(COLUMN_RESULT)),
                    cursor.getString(
                        cursor.getColumnIndex(
                            COLUMN_PLAYER_FIELD
                        )
                    ),
                    cursor.getString(cursor.getColumnIndex(COLUMN_OPPONENT_FIELD)),
                    cursor.getInt(
                        cursor.getColumnIndex(
                            COLUMN_DURATION
                        )
                    ),
                    cursor.getString(cursor.getColumnIndex(COLUMN_OPPONENT_NAME))
                )
                matchResults.add(matchResult)
            } while (cursor.moveToNext())
        }

        db.close()
        cursor.close()

        return matchResults
    }

    fun getMatchesCount(): Int {
        val counQuery = "SELECT  * FROM $TABLE_NAME"
        val db = this.readableDatabase
        val cursor = db.rawQuery(counQuery, null)

        val count = cursor.count
        cursor.close()

        return count
    }

    fun deleteMatchResult(matchResult: BattleshipMatchResult) {
        val db = this.writableDatabase
        db.delete(TABLE_NAME, "$COLUMN_ID = ?", arrayOf(matchResult.id.toString()))
        db.close()
    }

    companion object {
        private const val DATABASE_NAME = "battleshipStatistics.db"
        private const val DATABASE_VERSION = 1

        const val TABLE_NAME = "matchResults"
        const val COLUMN_OPPONENT_NAME = "opponentName"
        const val COLUMN_DURATION = "duration"
        const val COLUMN_OPPONENT_FIELD = "opponentField"
        const val COLUMN_PLAYER_FIELD = "playerField"
        const val COLUMN_ID = "id"
        const val COLUMN_RESULT = "result"
    }

}