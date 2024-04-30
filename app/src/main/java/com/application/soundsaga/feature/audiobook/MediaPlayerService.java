package com.application.soundsaga.feature.audiobook;

import static android.media.MediaPlayer.MEDIA_ERROR_IO;
import static android.media.MediaPlayer.MEDIA_ERROR_MALFORMED;
import static android.media.MediaPlayer.MEDIA_ERROR_SERVER_DIED;
import static android.media.MediaPlayer.MEDIA_ERROR_TIMED_OUT;
import static android.media.MediaPlayer.MEDIA_ERROR_UNKNOWN;
import static android.media.MediaPlayer.MEDIA_ERROR_UNSUPPORTED;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.application.soundsaga.MediaApp;
import com.application.soundsaga.R;
import com.application.soundsaga.common.data.model.AudioBook;
import com.application.soundsaga.common.data.model.Chapter;
import com.application.soundsaga.common.data.model.MyAudioBook;
import com.application.soundsaga.common.database.DatabaseRepository;
import com.application.soundsaga.common.dialog.AlertDialogs;
import com.application.soundsaga.common.util.Constants;
import com.application.soundsaga.common.util.Util;
import com.application.soundsaga.feature.main.MainActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.NotificationTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import kotlin.Suppress;

public class MediaPlayerService extends Service implements MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener {

    public static MediaPlayerService mediaPlayerService = null;
    public LocalBroadcastManager localBroadcastManager;
    private final MediaPlayerServiceBinder binder = new MediaPlayerServiceBinder();


    private RemoteViews notificationLayout;
    private static final int MEDIA_PLAYER_NOTIFICATION_ID = 1;


    private AudioBook audioBook;
    private Integer currentPosition = 0;
    private int durationAt = 0;
    private int currentAudioBookId = -1;
    private long currentTimestamp = -1;


    private boolean isPlaying = false;
    private boolean isPaused = false;
    public boolean isPrepared = false;
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private Runnable mediaPlayerRunnable;
    private final Handler mediaPlayerHandler = new Handler();



    public class MediaPlayerServiceBinder extends Binder {
        public MediaPlayerService getService() {
            return MediaPlayerService.this;
        }
    }

    public void setAudioBook(AudioBook audioBook) {
        this.audioBook = audioBook;
    }

    public AudioBook getAudioBook() {
        return audioBook;
    }

    public void setCurrentPosition(int position) {
        this.currentPosition = position;
    }

    public void setDurationAt(int durationAt) {
        this.durationAt = durationAt;
    }

    public void setCurrentAudioBookId(int currentAudioBookId) {
        this.currentAudioBookId = currentAudioBookId;
    }

    public void setCurrentTimestamp(long currentTimestamp) {
        this.currentTimestamp = currentTimestamp;
    }

    public Integer getCurrentPosition() {
        return currentPosition;
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    private void handleMediaPlayerUpdate() {
        if (mediaPlayerRunnable == null)
            mediaPlayerRunnable = new Runnable() {
                @Override
                public void run() {
                    if (mediaPlayer != null) {
                        sendMessageViaBroadcastManager(Constants.UPDATE_PROGRESS_MSG);
                    }
                    mediaPlayerHandler.postDelayed(this, 1000);

                }
            };
        mediaPlayerHandler.post(mediaPlayerRunnable);
    }

    private void sendMessageViaBroadcastManager(String msg) {
        Intent intent = new Intent(Constants.ACTIVITY_BROADCAST_INTENT_KEY);
        intent.putExtra(Constants.MESSAGE_KEY, msg);
        localBroadcastManager.sendBroadcast(intent);
    }

    public void createNotificationLayout() {
        notificationLayout = new RemoteViews(getPackageName(), R.layout.player_notification);

        Intent rewindClickedIntent = new Intent(Constants.ACTIONS_INTENT_KEY);
        rewindClickedIntent.putExtra(Constants.BUTTON_CLICKED_MSG, Constants.REWIND_INTENT_ACTION);
        PendingIntent rewindClickedPIntent = PendingIntent.getBroadcast(this, Constants.REWIND_INTENT_REQUEST_CODE, rewindClickedIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        notificationLayout.setOnClickPendingIntent(R.id.ivRewindButton, rewindClickedPIntent);

        Intent playPauseClickedIntent = new Intent(Constants.ACTIONS_INTENT_KEY);
        playPauseClickedIntent.putExtra(Constants.BUTTON_CLICKED_MSG, Constants.PLAY_PAUSE_INTENT_ACTION);
        PendingIntent playPauseClickedPIntent = PendingIntent.getBroadcast(this, Constants.PLAY_PAUSE_INTENT_REQUEST_CODE, playPauseClickedIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        notificationLayout.setOnClickPendingIntent(R.id.ivPlayPause, playPauseClickedPIntent);

        Intent forwardClickedIntent = new Intent(Constants.ACTIONS_INTENT_KEY);
        forwardClickedIntent.putExtra(Constants.BUTTON_CLICKED_MSG, Constants.FORWARD_INTENT_ACTION);
        PendingIntent forwardClickedPIntent = PendingIntent.getBroadcast(this, Constants.FORWARD_INTENT_REQUEST_CODE, forwardClickedIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        notificationLayout.setOnClickPendingIntent(R.id.ivForwardButton, forwardClickedPIntent);

        Intent closeClickedIntent = new Intent(Constants.ACTIONS_INTENT_KEY);
        closeClickedIntent.putExtra(Constants.BUTTON_CLICKED_MSG, Constants.CLOSE_INTENT_ACTION);
        PendingIntent closeClickedPIntent = PendingIntent.getBroadcast(this, Constants.CLOSE_INTENT_REQUEST_CODE, closeClickedIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        notificationLayout.setOnClickPendingIntent(R.id.ivCloseButton, closeClickedPIntent);

    }

    public Notification createNotification(int playPauseResId) {
        Intent notificationIntent = new Intent(this, AudioBookActivity.class);
        notificationIntent.putExtra(Constants.FROM_NOTIFICATION_INTENT, true);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (audioBook != null) {
            notificationLayout.setTextViewText(R.id.tvAudioBookTitle, audioBook.getTitle());
            notificationLayout.setTextViewText(R.id.tvAudioBookAuthor, audioBook.getContents().get(currentPosition).getTitle());
        } else {
            notificationLayout.setTextViewText(R.id.tvAudioBookTitle, getText(R.string.default_title));
            notificationLayout.setTextViewText(R.id.tvAudioBookAuthor, getText(R.string.default_author));
        }

        notificationLayout.setImageViewResource(R.id.ivPlayPause, playPauseResId);

        return new NotificationCompat.Builder(this, MediaApp.NOTIFICATION_CHANNEL_ID)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentTitle(getString(R.string.app_name))
                .setSmallIcon(R.drawable.baseline_audiotrack_black_18)
                .setOngoing(true)
                .setSilent(true)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(notificationLayout)
                .setContentIntent(pendingIntent)
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
                .build();
    }

    private void updateNotification(int playPauseResId) {
        Notification notification = createNotification(playPauseResId);
        notification.flags = Notification.FLAG_ONGOING_EVENT;
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager != null)
            notificationManager.notify(MEDIA_PLAYER_NOTIFICATION_ID, notification);
    }

    private void initMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnPreparedListener(this);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification notification = createNotification(R.drawable.baseline_play_circle_24);
        notification.flags = Notification.FLAG_ONGOING_EVENT;
        startForeground(MEDIA_PLAYER_NOTIFICATION_ID, notification);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        mediaPlayerService = this;
        initMediaPlayer();
        createNotificationLayout();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(actionsReceiver, new IntentFilter(Constants.ACTIONS_INTENT_KEY), Context.RECEIVER_EXPORTED);
        } else {
            registerReceiver(actionsReceiver, new IntentFilter(Constants.ACTIONS_INTENT_KEY));
        }
    }


    public boolean isPlaying() {
        return isPlaying;
    }

    public boolean isPaused() {
        return isPaused;
    }


    public void playMedia() {
        if (mediaPlayer != null && audioBook != null)
            if (Util.isNetworkAvailable(getApplication())) {
                try {
                    isPrepared = false;
                    if (mediaPlayer.isPlaying()) mediaPlayer.stop();
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource(audioBook.getContents().get(currentPosition).getUrl());
                    mediaPlayer.prepareAsync();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                showToast(getString(R.string.no_internet));
            }
    }

    public void playOrPauseMedia() {
        if (audioBook != null) {
            if (mediaPlayer.isPlaying()) {
                pauseTrack();
            } else {
                mediaPlayer.start();
                mediaPlayerHandler.removeCallbacks(mediaPlayerRunnable);
                handleMediaPlayerUpdate();
                isPlaying = true;
                isPaused = false;
                updateNotification(R.drawable.baseline_pause_circle_24);
                sendMessageViaBroadcastManager(Constants.PLAYED_MSG);
            }
        }
    }

    private void pauseTrack() {
        mediaPlayer.pause();
        isPlaying = false;
        isPaused = true;
        updateNotification(R.drawable.baseline_play_circle_24);
        sendMessageViaBroadcastManager(Constants.PAUSED_MSG);
    }

    private final BroadcastReceiver actionsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getStringExtra(Constants.BUTTON_CLICKED_MSG);
            switch (action) {
                case Constants.REWIND_INTENT_ACTION:
                    rewindTrack();
                    break;
                case Constants.PLAY_PAUSE_INTENT_ACTION:
                    playOrPauseMedia();
                    break;
                case Constants.FORWARD_INTENT_ACTION:
                    forwardTrack();
                    break;
                case Constants.CLOSE_INTENT_ACTION:
                    close();
                    break;

            }
        }
    };

    public void rewindTrack() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() - Constants.REWIND_FORWARD_TRACK_BY_MILLISECONDS);
        }
    }


    public void forwardTrack() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() + Constants.REWIND_FORWARD_TRACK_BY_MILLISECONDS);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaPlayerHandler.removeCallbacks(mediaPlayerRunnable);
        closeMediaPlayer();
        sendMessageViaBroadcastManager(Constants.SERVICE_DESTROY_MSG);
        unregisterReceiver(actionsReceiver);
    }

    private void closeMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    private void saveLastDurationToDatabase() {
        if (mediaPlayer != null && mediaPlayer.getCurrentPosition() > 0) {
            MyAudioBook myAudioBook = new MyAudioBook();
            if (currentAudioBookId != -1) myAudioBook.setId(currentAudioBookId);
            AudioBook newAudioBook = audioBook;
            List<Chapter> chapters = new ArrayList<>();
            chapters.add(audioBook.getContents().get(currentPosition));
            newAudioBook.setContents(chapters);
            myAudioBook.setAudioBook(newAudioBook);
            myAudioBook.setDurationAt(mediaPlayer.getCurrentPosition());
            myAudioBook.setTotalDuration(mediaPlayer.getDuration());
            if (currentTimestamp == -1)
                myAudioBook.setLastPlayingTimestamp(System.currentTimeMillis());
            else myAudioBook.setLastPlayingTimestamp(currentTimestamp);
            DatabaseRepository.getInstance(getApplicationContext()).insertOrUpdate(myAudioBook);
        }
    }


    private void onCompleted() {
        if (currentPosition < audioBook.getContents().size() - 1) {
            currentPosition++;
            sendMessageViaBroadcastManager(Constants.COMPLETION_MSG);
            playMedia();
        } else {
            isPaused = true;
            sendMessageViaBroadcastManager(Constants.PAUSED_MSG);
            updateNotification(R.drawable.baseline_play_circle_24);
        }
    }


    @Override
    public void onCompletion(MediaPlayer mp) {
        onCompleted();
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
        mp.seekTo(durationAt);
        updateNotification(R.drawable.baseline_pause_circle_24);
        isPrepared = true;
        isPlaying = true;

        mediaPlayerHandler.removeCallbacks(mediaPlayerRunnable);
        handleMediaPlayerUpdate();
        sendMessageViaBroadcastManager(Constants.PREPARED_MSG);

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        if (what == MEDIA_ERROR_UNKNOWN) {
            showToast("MEDIA_ERROR_UNKNOWN");
        } else if (what == MEDIA_ERROR_SERVER_DIED) {
            showToast("MEDIA_ERROR_SERVER_DIED");
        }
        if (extra == MEDIA_ERROR_IO) {
            showToast("MEDIA_ERROR_IO");
        } else if (extra == MEDIA_ERROR_MALFORMED) {
            showToast("MEDIA_ERROR_MALFORMED");
        } else if (extra == MEDIA_ERROR_UNSUPPORTED) {
            showToast("MEDIA_ERROR_UNSUPPORTED");
        } else if (extra == MEDIA_ERROR_TIMED_OUT) {
            showToast("MEDIA_ERROR_TIMED_OUT");
        }
        return true;
    }

    private void showToast(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
    }

    public void close() {
        saveLastDurationToDatabase();

        mediaPlayerService = null;
        sendMessageViaBroadcastManager(Constants.UNBIND_REQUEST_MSG);
        mediaPlayerHandler.removeCallbacks(mediaPlayerRunnable);
        closeMediaPlayer();
        stopForeground(true);
        stopSelf();
    }

}


