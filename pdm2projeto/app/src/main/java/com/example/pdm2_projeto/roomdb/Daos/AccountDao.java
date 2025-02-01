package com.example.pdm2_projeto.roomdb.Daos;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.pdm2_projeto.models.Account;

@Dao
public interface AccountDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Account account);

    @Query("SELECT lastLoginTimestamp FROM account ORDER BY id DESC LIMIT 1")
    Long getLastLoginTimestamp();
}
