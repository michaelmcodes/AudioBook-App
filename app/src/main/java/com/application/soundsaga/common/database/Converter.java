package com.application.soundsaga.common.database;


import androidx.room.TypeConverter;

import com.application.soundsaga.common.data.model.AudioBook;
import com.application.soundsaga.common.data.model.Chapter;
import com.google.gson.Gson;

public class Converter {
    Gson gson = new Gson();

    @TypeConverter
    public String fromAudioBook(AudioBook audioBook) {
        return gson.toJson(audioBook);
    }

    @TypeConverter
    public AudioBook toAudioBook(String data) {
        return gson.fromJson(data, AudioBook.class);
    }
}
