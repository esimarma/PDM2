package com.example.pdm2_projeto.roomdb;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.pdm2_projeto.models.Account;
import com.example.pdm2_projeto.roomdb.Daos.AccountDao;

/**
 * Room Database class for managing the local SQLite database.
 * This class follows the Singleton pattern to ensure a single instance of the database.
 */
@Database(entities = {Account.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    /**
     * Singleton instance of the database to prevent multiple instances causing resource leaks.
     */
    private static volatile AppDatabase INSTANCE;

    /**
     * Abstract method to access the AccountDao interface for database operations.
     *
     * @return Instance of AccountDao.
     */
    public abstract AccountDao accountDao();

    /**
     * Returns the singleton instance of the database.
     * If the instance does not exist, it initializes the database with Room.
     *
     * @param context The application context used to build the database instance.
     * @return The singleton instance of AppDatabase.
     */
    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "account_database")
                            .fallbackToDestructiveMigration() // Allows migration strategy for database schema changes.
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}

