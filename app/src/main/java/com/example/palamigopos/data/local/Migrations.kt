package com.example.palamigopos.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migrations {
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE products ADD COLUMN isActive INTEGER NOT NULL DEFAULT 1")
        }
    }

    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE orders ADD COLUMN paymentMethod TEXT NOT NULL DEFAULT 'Cash'")
        }
    }

    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS categories (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    name TEXT NOT NULL,
                    createdAt INTEGER NOT NULL
                )
            """)
            // Seed initial categories
            db.execSQL("INSERT INTO categories (name, createdAt) VALUES ('Coffee', ${System.currentTimeMillis()})")
            db.execSQL("INSERT INTO categories (name, createdAt) VALUES ('Milk Creation', ${System.currentTimeMillis()})")
            db.execSQL("INSERT INTO categories (name, createdAt) VALUES ('Soda Spark', ${System.currentTimeMillis()})")
        }
    }
}
