package com.application.soundsaga.common.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.TypeConverter;
import androidx.room.Update;

import com.application.soundsaga.common.data.model.AudioBook;
import com.application.soundsaga.common.data.model.MyAudioBook;
import com.application.soundsaga.common.util.Constants;

import java.util.List;

@Dao
public interface MyAudioBookDao {
    @Query("SELECT * FROM " + Constants.DATABASE_NAME + " ORDER BY lastPlayingTimestamp DESC")
    List<MyAudioBook> getAll();

    @Query("SELECT * FROM " + Constants.DATABASE_NAME + " WHERE audioBook LIKE '%' || :audioBookTitle || '%'")
    MyAudioBook getMyAudioBook(String audioBookTitle);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(MyAudioBook myAudioBook);

    @Update
    void update(MyAudioBook myAudioBook);

    @Delete
    void delete(MyAudioBook myAudioBook);

}
