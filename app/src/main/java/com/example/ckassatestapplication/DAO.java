package com.example.ckassatestapplication;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface DAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void save(DictionaryEntry entry);

    @Update
    void update(DictionaryEntry entry);

    @Delete
    void delete(DictionaryEntry entry);

    @Query("DELETE FROM DictionaryEntry WHERE  id = :id")
    void delete(int id);

    @Query("SELECT * FROM DictionaryEntry")
    List<DictionaryEntry> findAll();

    @Query("SELECT * FROM DictionaryEntry WHERE  id = :id")
    List<DictionaryEntry> findById(int id);
}