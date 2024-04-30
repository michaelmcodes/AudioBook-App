package com.application.soundsaga.common.data.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.application.soundsaga.common.database.Converter;
import com.application.soundsaga.common.util.Constants;

@Entity(tableName = Constants.DATABASE_NAME)
public class MyAudioBook {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "audioBook")
    @TypeConverters(Converter.class)
    private AudioBook audioBook;
    @ColumnInfo(name = "durationAt")
    private int durationAt;
    @ColumnInfo(name = "totalDuration")
    private int totalDuration;
    @ColumnInfo(name = "lastPlayingTimestamp")
    private long lastPlayingTimestamp;
    public void setAudioBook(AudioBook audioBook) {
        this.audioBook = audioBook;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setDurationAt(int durationAt) {
        this.durationAt = durationAt;
    }

    public void setLastPlayingTimestamp(long lastPlayingTimestamp) {
        this.lastPlayingTimestamp = lastPlayingTimestamp;
    }

    public void setTotalDuration(int totalDuration) {
        this.totalDuration = totalDuration;
    }

    public int getId() {
        return id;
    }

    public AudioBook getAudioBook() {
        return audioBook;
    }

    public int getDurationAt() {
        return durationAt;
    }

    public int getTotalDuration() {
        return totalDuration;
    }

    public long getLastPlayingTimestamp() {
        return lastPlayingTimestamp;
    }
}
