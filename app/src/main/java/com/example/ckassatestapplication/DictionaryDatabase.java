package com.example.ckassatestapplication;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

@Database(entities = {DictionaryEntry.class}, version = 1)
public abstract class DictionaryDatabase extends RoomDatabase {
    private static DictionaryDatabase mDictionaryDatabase;

    public static DictionaryDatabase getInstance(Context context) {
        if (mDictionaryDatabase == null) {
            mDictionaryDatabase = Room.databaseBuilder(context, DictionaryDatabase.class, "dictionary").build();
        }
        return mDictionaryDatabase;
    }

    public abstract DAO dao();
}