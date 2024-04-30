package com.application.soundsaga.common.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;

import java.util.List;

public class AudioBook {

    private String title;
    private String author;
    private String date;
    private String language;
    private String duration;
    private String image;

    private List<Chapter> contents;

    public String getTitle() {
        return title;
    }

    public String getImage() {
        return image;
    }

    public List<Chapter> getContents() {
        return contents;
    }

    public String getAuthor() {
        return author;
    }

    public String getDate() {
        return date;
    }

    public String getDuration() {
        return duration;
    }

    public String getLanguage() {
        return language;
    }

    public void setContents(List<Chapter> contents) {
        this.contents = contents;
    }
}
