package com.application.soundsaga.common.util;

public class Constants {

    public static final String INTENT_AUDIO_BOOK = "AudioBookIntent";
    public static final String INTENT_AUDIO_BOOK_CHAPTER_DURATION_AT = "AudioBookChapterDurationAt";
    public static final String INTENT_MY_AUDIO_BOOK_ID= "MyAudioBookId";
    public static final String INTENT_MY_AUDIO_BOOK_TIMESTAMP= "MyAudioBookTimestamp";

    public static final String DATABASE_NAME = "my_audio_books";

    public static final int REWIND_FORWARD_TRACK_BY_MILLISECONDS = 15000;
    public static final String ACTIVITY_BROADCAST_INTENT_KEY = "MusicForegroundServiceToActivity";
    public static final String ACTIONS_INTENT_KEY = "ACTIONS_INTENT_KEY";
    public static final String MESSAGE_KEY = "MESSAGE_KEY";
    public static final String PREPARED_MSG = "PREPARED_MSG";
    public static final String COMPLETION_MSG = "COMPLETION_MSG";
    public static final String FROM_NOTIFICATION_INTENT = "FROM_NOTIFICATION_INTENT";
    public static final String UPDATE_PROGRESS_MSG = "UPDATE_PROGRESS_MSG";
    public static final String UNBIND_REQUEST_MSG = "UNBIND_REQUEST_MSG";
    public static final String SERVICE_DESTROY_MSG = "SERVICE_DESTROY_MSG";
    public static final String PAUSED_MSG = "PAUSED_MSG";
    public static final String PLAYED_MSG = "PLAYED_MSG";

    public static final String REWIND_INTENT_ACTION = "rewind";
    public static final int REWIND_INTENT_REQUEST_CODE = 1;
    public static final int PLAY_PAUSE_INTENT_REQUEST_CODE = 2;
    public static final int FORWARD_INTENT_REQUEST_CODE = 3;
    public static final int CLOSE_INTENT_REQUEST_CODE = 4;
    public static final String PLAY_PAUSE_INTENT_ACTION = "play_pause";
    public static final String FORWARD_INTENT_ACTION = "forward";
    public static final String CLOSE_INTENT_ACTION = "close";
    public static final String BUTTON_CLICKED_MSG = "notification button";

}
