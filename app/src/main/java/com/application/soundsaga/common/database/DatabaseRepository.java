package com.application.soundsaga.common.database;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.room.Room;

import com.application.soundsaga.common.data.model.MyAudioBook;
import com.application.soundsaga.common.util.Constants;
import com.google.gson.Gson;

import java.util.List;

public class DatabaseRepository {

    private final AppDatabase database;

    public DatabaseRepository(Context context) {
        database = Room.databaseBuilder(context,
                AppDatabase.class, Constants.DATABASE_NAME).build();
    }

    private static volatile DatabaseRepository INSTANCE = null;

    public static DatabaseRepository getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (DatabaseRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new DatabaseRepository(context);
                }
            }
        }
        return INSTANCE;
    }

    public List<MyAudioBook> getAll() {
        return database.myAudioBookDao().getAll();
    }
    public void insertOrUpdate(MyAudioBook myAudioBook) {
        new Thread(() -> database.runInTransaction(() -> {
            MyAudioBook dbMyAudioBook = database.myAudioBookDao().getMyAudioBook(myAudioBook.getAudioBook().getTitle());
            if (dbMyAudioBook == null) {
                database.myAudioBookDao().insert(myAudioBook);
            } else {
                myAudioBook.setId(dbMyAudioBook.getId());
                database.myAudioBookDao().update(myAudioBook);
            }
        })).start();
    }
    public void delete(MyAudioBook myAudioBook) {
        new Thread(() -> database.myAudioBookDao().delete(myAudioBook)).start();
    }

}
