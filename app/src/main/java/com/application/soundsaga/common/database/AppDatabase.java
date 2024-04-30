package com.application.soundsaga.common.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.application.soundsaga.common.data.model.MyAudioBook;

@Database(entities = {MyAudioBook.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract MyAudioBookDao myAudioBookDao();
}