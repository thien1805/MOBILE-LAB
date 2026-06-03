package com.example.lab04inclass

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "DictionaryDB"
        private const val DATABASE_VERSION = 1
        const val TABLE_NAME = "Dictionary"
        const val COLUMN_ID = "id"
        const val COLUMN_WORD = "word"
        const val COLUMN_DEFINITION = "definition"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        // Create the Dictionary table
        val createTableQuery = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_WORD TEXT NOT NULL UNIQUE,
                $COLUMN_DEFINITION TEXT NOT NULL
            )
        """.trimIndent()
        
        db?.execSQL(createTableQuery)
        
        // Seed the database with sample data on first launch
        seedDatabase(db)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // Drop existing table if it exists
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    /**
     * Seed the database with sample dictionary words and definitions
     */
    private fun seedDatabase(db: SQLiteDatabase?) {
        val sampleData = listOf(
            Pair("android", "A mobile operating system based on the Linux kernel."),
            Pair("kotlin", "A statically typed programming language for modern multiplatform applications."),
            Pair("database", "An organized collection of structured data stored and accessed electronically."),
            Pair("dictionary", "A collection of words and phrases with their definitions and meanings."),
            Pair("compose", "A modern toolkit for building native Android UI with less code."),
            Pair("fragment", "A reusable piece of your app's user interface."),
            Pair("activity", "A single screen with a user interface in an Android application."),
            Pair("recyclerview", "A flexible view for providing a limited window into a large data set."),
            Pair("gradient", "A gradual blending from one color to another."),
            Pair("constraint", "A rule or limitation that restricts the position or size of UI elements.")
        )

        sampleData.forEach { (word, definition) ->
            val contentValues = ContentValues().apply {
                put(COLUMN_WORD, word)
                put(COLUMN_DEFINITION, definition)
            }
            try {
                db?.insert(TABLE_NAME, null, contentValues)
            } catch (e: Exception) {
                // Word might already exist (unique constraint)
                e.printStackTrace()
            }
        }
    }

    /**
     * Search for exact match (case-insensitive)
     * Returns the definition if found, null otherwise
     */
    fun searchExactMatch(word: String): String? {
        val db = readableDatabase
        val cursor: Cursor = db.query(
            TABLE_NAME,
            arrayOf(COLUMN_DEFINITION),
            "$COLUMN_WORD = ?",
            arrayOf(word.lowercase()),
            null,
            null,
            null
        )

        var result: String? = null
        cursor.use {
            if (it.moveToFirst()) {
                result = it.getString(it.getColumnIndexOrThrow(COLUMN_DEFINITION))
            }
        }
        return result
    }

    /**
     * Search for substring matches (case-insensitive)
     * Returns a list of words that contain the search term
     */
    fun searchSubstringMatches(searchTerm: String): List<String> {
        val db = readableDatabase
        val cursor: Cursor = db.query(
            TABLE_NAME,
            arrayOf(COLUMN_WORD),
            "$COLUMN_WORD LIKE ?",
            arrayOf("%${searchTerm.lowercase()}%"),
            null,
            null,
            null
        )

        val matches = mutableListOf<String>()
        cursor.use {
            while (it.moveToNext()) {
                val word = it.getString(it.getColumnIndexOrThrow(COLUMN_WORD))
                matches.add(word)
            }
        }
        return matches
    }

    /**
     * Insert a new word into the dictionary
     * (Available for future use in extending the dictionary)
     */
    @Suppress("UNUSED")
    fun insertWord(word: String, definition: String): Long {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_WORD, word.lowercase())
            put(COLUMN_DEFINITION, definition)
        }
        return db.insert(TABLE_NAME, null, contentValues)
    }

    /**
     * Get all words in the dictionary
     * (Available for future use such as browsing all words)
     */
    @Suppress("UNUSED")
    fun getAllWords(): List<String> {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_NAME,
            arrayOf(COLUMN_WORD),
            null,
            null,
            null,
            null,
            "$COLUMN_WORD ASC"
        )

        val words = mutableListOf<String>()
        cursor.use {
            while (it.moveToNext()) {
                val word = it.getString(it.getColumnIndexOrThrow(COLUMN_WORD))
                words.add(word)
            }
        }
        return words
    }
}


