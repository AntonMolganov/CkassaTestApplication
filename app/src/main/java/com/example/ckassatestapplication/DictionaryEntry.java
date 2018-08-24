package com.example.ckassatestapplication;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity
public class DictionaryEntry implements Comparable<DictionaryEntry>{

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int id;
    @ColumnInfo(name = "source")
    private String source;
    @ColumnInfo(name = "translation")
    private String translation;

    DictionaryEntry() {
    }

    @Ignore
    DictionaryEntry(int id, String source, String translation) {
        this.id = id;
        this.source = source;
        this.translation = translation;
    }

    @Ignore
    DictionaryEntry(String source, String translation) {
        this.id = -1;
        this.source = source;
        this.translation = translation;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTranslation() {
        return translation;
    }

    public void setTranslation(String translation) {
        this.translation = translation;
    }

    @Override
    public int compareTo(@NonNull DictionaryEntry e) {
        return this.source.compareTo(e.source);
    }
}
