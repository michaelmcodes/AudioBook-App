package com.application.soundsaga.common.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;

public class Chapter {
    private int number;
    private String title;
    private String url;

    public int getNumber() {
        return number;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }
}

