package com.example.pdm2_projeto.roomdb.Daos;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.pdm2_projeto.models.Account;

/**
 * Data Access Object (DAO) interface for managing Account data in Room Database.
 * Provides methods to insert and retrieve account-related information.
 */
@Dao
public interface AccountDao {

    /**
     * Inserts an account record into the database.
     * If an account with the same primary key already exists, it will be replaced.
     *
     * @param account The Account object to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Account account);

    /**
     * Retrieves the last login timestamp from the most recent account record.
     *
     * @return The timestamp of the last login, or null if no records exist.
     */
    @Query("SELECT lastLoginTimestamp FROM account ORDER BY id DESC LIMIT 1")
    Long getLastLoginTimestamp();
}

