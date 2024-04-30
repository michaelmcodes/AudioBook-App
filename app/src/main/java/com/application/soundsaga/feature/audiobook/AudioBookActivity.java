package com.application.soundsaga.feature.audiobook;

import static android.media.MediaPlayer.MEDIA_ERROR_IO;
import static android.media.MediaPlayer.MEDIA_ERROR_MALFORMED;
import static android.media.MediaPlayer.MEDIA_ERROR_SERVER_DIED;
import static android.media.MediaPlayer.MEDIA_ERROR_TIMED_OUT;
import static android.media.MediaPlayer.MEDIA_ERROR_UNKNOWN;
import static android.media.MediaPlayer.MEDIA_ERROR_UNSUPPORTED;
import static android.media.MediaPlayer.MEDIA_INFO_UNSUPPORTED_SUBTITLE;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.lifecycle.Observer;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager2.widget.ViewPager2;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.window.OnBackInvokedDispatcher;

import com.application.soundsaga.R;
import com.application.soundsaga.common.data.model.AudioBook;
import com.application.soundsaga.common.data.model.Chapter;
import com.application.soundsaga.common.data.model.MyAudioBook;
import com.application.soundsaga.common.database.DatabaseRepository;
import com.application.soundsaga.common.dialog.AlertDialogs;
import com.application.soundsaga.common.util.Constants;
import com.application.soundsaga.common.util.Util;
import com.application.soundsaga.databinding.ActivityAudioBookBinding;
import com.google.gson.Gson;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class AudioBookActivity extends AppCompatActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private ActivityAudioBookBinding binding;
    private final AudioBookChaptersViewPagerAdapter adapter = new AudioBookChaptersViewPagerAdapter();
    private AudioBook audioBook;
    private Integer currentPosition;
    private int durationAt = 0;
    private int currentAudioBookId = -1;
    private long currentTimestamp = -1;
    private ViewPager2.OnPageChangeCallback onPageChangeCallback;

    private MediaPlayerService mediaPlayerService = null;
    private boolean mIsBound;
    private boolean isStarted = true;

    private boolean allowedToPlay = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAudioBookBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (getIntent() != null) {
            durationAt = getIntent().getIntExtra(Constants.INTENT_AUDIO_BOOK_CHAPTER_DURATION_AT, 0);
            currentAudioBookId = getIntent().getIntExtra(Constants.INTENT_MY_AUDIO_BOOK_ID, -1);
            currentTimestamp = getIntent().getLongExtra(Constants.INTENT_MY_AUDIO_BOOK_TIMESTAMP, -1);
            String jsonAudioBook = getIntent().getStringExtra(Constants.INTENT_AUDIO_BOOK);
            if (jsonAudioBook != null) {
                setupAudioBook(new Gson().fromJson(jsonAudioBook, AudioBook.class));
            }
        }
        getOnBackPressedDispatcher().addCallback(onBackPressedCallback);


    }

    private final OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            MediaPlayerService.mediaPlayerService.close();
            finish();
        }
    };


    private final BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String msg = intent.getStringExtra(Constants.MESSAGE_KEY);
            switch (msg) {
                case Constants.PREPARED_MSG:
                    onPrepared();
                    break;
                case Constants.COMPLETION_MSG:
                    onCompleted();
                    break;
                case Constants.UPDATE_PROGRESS_MSG:
                    int position = mediaPlayerService.getMediaPlayer().getCurrentPosition();
                    binding.sbMediaPlayer.setProgress(position);
                    binding.tvDurationAt.setText(Util.humanizeMilliseconds(position));
                    break;
                case Constants.PAUSED_MSG:
                    binding.ivPlayPause.setImageResource(R.drawable.play);
                    break;
                case Constants.PLAYED_MSG:
                    binding.ivPlayPause.setImageResource(R.drawable.pause);
                    break;
                case Constants.UNBIND_REQUEST_MSG:
                    unbind();
                    break;
                case Constants.SERVICE_DESTROY_MSG:
                    onServiceDestroy();
                    break;
            }
        }
    };

    private void unbind() {
        if (mIsBound) {
            mediaPlayerService = null;
            mIsBound = false;
            audioBook = null;
            currentPosition = 0;
            unbindService(mConnection);
        }
    }

    private void onServiceDestroy() {
        finish();
    }

    private void initBroadcastReceiver() {
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, new IntentFilter(Constants.ACTIVITY_BROADCAST_INTENT_KEY));
    }

    private void setService() {
        Intent startMusicService = new Intent(this, MediaPlayerService.class);
        if (MediaPlayerService.mediaPlayerService == null)
            getApplication().startService(startMusicService);
        bindService(startMusicService, mConnection, Context.BIND_AUTO_CREATE);

    }

    private void onCompleted() {
        currentPosition = mediaPlayerService.getCurrentPosition();
        binding.rvChapters.setCurrentItem(currentPosition, true);
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (mIsBound) unbind();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReceiver);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (mediaPlayerService == null) {
            setService();
        }
        initBroadcastReceiver();
    }

    private final ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {

            MediaPlayerService.MediaPlayerServiceBinder binder = (MediaPlayerService.MediaPlayerServiceBinder) service;
            mediaPlayerService = binder.getService();
            mIsBound = true;

            mediaPlayerService.setCurrentAudioBookId(currentAudioBookId);
            mediaPlayerService.setCurrentTimestamp(currentTimestamp);
            mediaPlayerService.setDurationAt(durationAt);

            if (audioBook != null) {
                if (isStarted) {
                    mediaPlayerService.setAudioBook(audioBook);
                    currentPosition = 0;
                    mediaPlayerService.setCurrentPosition(0);
                    play();
                    isStarted = false;
                }
            } else {
                setupAudioBook(mediaPlayerService.getAudioBook());
                currentPosition = mediaPlayerService.getCurrentPosition();
                allowedToPlay = false;
                binding.rvChapters.setCurrentItem(currentPosition, false);
                restoreAudioBookInfo();
            }

            setPlaybackSpeedBasedOnPlaybackParams();


        }

        public void onServiceDisconnected(ComponentName className) {
            mediaPlayerService = null;
            mIsBound = false;
        }
    };


    private void setupAudioBook(AudioBook audioBook) {
        this.audioBook = audioBook;
        initAudioBook();
        initViews();
    }

    private void onPrepared() {
        if (mediaPlayerService.isPrepared) {
            binding.ivPlayPause.setImageResource(R.drawable.pause);
            binding.sbMediaPlayer.setMax(mediaPlayerService.getMediaPlayer().getDuration());
            binding.tvTotalDuration.setText(Util.humanizeMilliseconds(mediaPlayerService.getMediaPlayer().getDuration()));
            setPlaybackSpeedBasedOnPlaybackParams();
        }
    }

    private void initViews() {
        binding.tvPlaybackSpeed.setText(Util.makeTextWithUnderline(getString(R.string.playback_speed_default)));
        binding.tvPlaybackSpeed.setOnClickListener(this);
        binding.ivPrev.setOnClickListener(this);
        binding.ivNext.setOnClickListener(this);
        binding.rlBackward.setOnClickListener(this);
        binding.rlForward.setOnClickListener(this);
        binding.ivPlayPause.setOnClickListener(this);

        binding.rvChapters.setAdapter(adapter);
        onPageChangeCallback = new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                onChapterSwiped(position);
            }
        };
        binding.rvChapters.registerOnPageChangeCallback(onPageChangeCallback);

        binding.sbMediaPlayer.setOnSeekBarChangeListener(this);
    }

    private void onChapterSwiped(int position) {
        currentPosition = position;

        binding.ivPrev.setVisibility(View.VISIBLE);
        binding.ivNext.setVisibility(View.VISIBLE);

        if (position == 0) {
            binding.ivPrev.setVisibility(View.INVISIBLE);
        }
        if (position == audioBook.getContents().size() - 1) {
            binding.ivNext.setVisibility(View.INVISIBLE);
        }

        if (mediaPlayerService != null) {
            if (allowedToPlay) {
                mediaPlayerService.setCurrentPosition(currentPosition);
                play();
            }
        }


    }

    private void play() {
        if (mediaPlayerService != null) {
            mediaPlayerService.playMedia();
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        restoreAudioBookInfo();
    }

    private void restoreAudioBookInfo() {
        if (mediaPlayerService != null && (mediaPlayerService.isPlaying() || mediaPlayerService.isPaused())) {
            binding.sbMediaPlayer.setMax(mediaPlayerService.getMediaPlayer().getDuration());
            binding.tvTotalDuration.setText(Util.humanizeMilliseconds(mediaPlayerService.getMediaPlayer().getDuration()));
            if (mediaPlayerService.isPlaying()) {
                binding.ivPlayPause.setImageResource(R.drawable.pause);
            } else if (mediaPlayerService.isPaused()) {
                binding.ivPlayPause.setImageResource(R.drawable.play);
            }
        }
    }

    private void initAudioBook() {
        adapter.setData(audioBook);
        binding.tvAudioBookTitle.setText(audioBook.getTitle());
        binding.tvAudioBookTitle.setSelected(true);
    }

    private void onPrev() {
        if (currentPosition > 0) {
            allowedToPlay = true;
            binding.rvChapters.setCurrentItem(currentPosition - 1, true);
        }

    }

    private void onNext() {
        if (currentPosition < audioBook.getContents().size() - 1) {
            allowedToPlay = true;
            binding.rvChapters.setCurrentItem(currentPosition + 1, true);
        }

    }

    private void onBackward() {
        mediaPlayerService.rewindTrack();
    }

    private void onForward() {
        mediaPlayerService.forwardTrack();
    }

    private void onPlayPause() {
        if (mediaPlayerService.getMediaPlayer() != null) {
            mediaPlayerService.playOrPauseMedia();
        }
    }

    private void showPlaybackSpeedPopup() {
        PopupMenu popupMenu = new PopupMenu(this, binding.tvPlaybackSpeed);
        popupMenu.getMenuInflater().inflate(R.menu.playback_speed_popup_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(menuItem -> {
            if (menuItem.getTitle() == null) return false;
            binding.tvPlaybackSpeed.setText(Util.makeTextWithUnderline(menuItem.getTitle().toString()));
            if (menuItem.getTitle().toString().equals(getString(R.string.playback_speed_0_75x))) {
                mediaPlayerService.getMediaPlayer().setPlaybackParams(mediaPlayerService.getMediaPlayer().getPlaybackParams().setSpeed(0.75f));
            } else if (menuItem.getTitle().toString().equals(getString(R.string.playback_speed_default))) {
                mediaPlayerService.getMediaPlayer().setPlaybackParams(mediaPlayerService.getMediaPlayer().getPlaybackParams().setSpeed(1f));
            } else if (menuItem.getTitle().toString().equals(getString(R.string.playback_speed_1_1x))) {
                mediaPlayerService.getMediaPlayer().setPlaybackParams(mediaPlayerService.getMediaPlayer().getPlaybackParams().setSpeed(1.1f));
            } else if (menuItem.getTitle().toString().equals(getString(R.string.playback_speed_1_25x))) {
                mediaPlayerService.getMediaPlayer().setPlaybackParams(mediaPlayerService.getMediaPlayer().getPlaybackParams().setSpeed(1.25f));
            } else if (menuItem.getTitle().toString().equals(getString(R.string.playback_speed_1_5x))) {
                mediaPlayerService.getMediaPlayer().setPlaybackParams(mediaPlayerService.getMediaPlayer().getPlaybackParams().setSpeed(1.5f));
            } else if (menuItem.getTitle().toString().equals(getString(R.string.playback_speed_1_75x))) {
                mediaPlayerService.getMediaPlayer().setPlaybackParams(mediaPlayerService.getMediaPlayer().getPlaybackParams().setSpeed(1.75f));
            } else if (menuItem.getTitle().toString().equals(getString(R.string.playback_speed_2_0x))) {
                mediaPlayerService.getMediaPlayer().setPlaybackParams(mediaPlayerService.getMediaPlayer().getPlaybackParams().setSpeed(2f));
            }
            return true;
        });
        popupMenu.show();
    }

    private void setPlaybackSpeedBasedOnPlaybackParams() {
        if (mediaPlayerService.getMediaPlayer() != null) {
            float speed = mediaPlayerService.getMediaPlayer().getPlaybackParams().getSpeed();
            String speedString = getString(R.string.playback_speed_default);
            ;
            if (speed == 0.75f) {
                speedString = getString(R.string.playback_speed_0_75x);
            } else if (speed == 1f) {
                speedString = getString(R.string.playback_speed_default);
            } else if (speed == 1.1f) {
                speedString = getString(R.string.playback_speed_1_1x);
            } else if (speed == 1.25f) {
                speedString = getString(R.string.playback_speed_1_25x);
            } else if (speed == 1.5f) {
                speedString = getString(R.string.playback_speed_1_5x);
            } else if (speed == 1.75f) {
                speedString = getString(R.string.playback_speed_1_75x);
            } else if (speed == 2f) {
                speedString = getString(R.string.playback_speed_2_0x);
            }

            binding.tvPlaybackSpeed.setText(Util.makeTextWithUnderline(speedString));
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tvPlaybackSpeed) {
            showPlaybackSpeedPopup();
        } else if (v.getId() == R.id.ivPrev) {
            onPrev();
        } else if (v.getId() == R.id.ivNext) {
            onNext();
        } else if (v.getId() == R.id.rlBackward) {
            onBackward();
        } else if (v.getId() == R.id.rlForward) {
            onForward();
        } else if (v.getId() == R.id.ivPlayPause) {
            onPlayPause();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding.rvChapters.unregisterOnPageChangeCallback(onPageChangeCallback);
    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (seekBar.getId() == R.id.sbMediaPlayer) {
            if (mediaPlayerService.getMediaPlayer() != null && mediaPlayerService.getMediaPlayer().isPlaying() && fromUser) {
                mediaPlayerService.getMediaPlayer().seekTo(progress);
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

}