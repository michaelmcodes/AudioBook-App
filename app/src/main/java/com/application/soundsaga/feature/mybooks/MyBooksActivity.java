package com.application.soundsaga.feature.mybooks;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import com.application.soundsaga.R;
import com.application.soundsaga.common.data.model.MyAudioBook;
import com.application.soundsaga.common.database.DatabaseRepository;
import com.application.soundsaga.common.dialog.AlertDialogs;
import com.application.soundsaga.common.util.Constants;
import com.application.soundsaga.databinding.ActivityMyBooksBinding;
import com.application.soundsaga.feature.audiobook.AudioBookActivity;
import com.google.gson.Gson;

import java.util.List;

public class MyBooksActivity extends AppCompatActivity implements MyAudioBooksRecyclerViewAdapter.OnItemClickListener, MyAudioBooksRecyclerViewAdapter.OnItemLongClickListener {

    private ActivityMyBooksBinding binding;
    private final MyAudioBooksRecyclerViewAdapter adapter = new MyAudioBooksRecyclerViewAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMyBooksBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getMyBooks();
    }

    private void getMyBooks() {
        new Thread(() -> {
            List<MyAudioBook> myBooks = DatabaseRepository.getInstance(getApplicationContext()).getAll();
            runOnUiThread(() -> adapter.setData(myBooks));

        }).start();
    }

    private void initViews() {
        binding.rvMyAudioBooks.setAdapter(adapter);
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            binding.rvMyAudioBooks.setLayoutManager(new GridLayoutManager(getApplicationContext(), 2));
        } else {
            binding.rvMyAudioBooks.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        }
        adapter.setOnItemClickListener(this);
        adapter.setOnItemLongClickListener(this);
    }

    @Override
    public void onItemClick(int position) {
        Intent intent = new Intent(this, AudioBookActivity.class);
        intent.putExtra(Constants.INTENT_AUDIO_BOOK, new Gson().toJson(adapter.getMyAudioBook(position).getAudioBook()));
        intent.putExtra(Constants.INTENT_AUDIO_BOOK_CHAPTER_DURATION_AT, adapter.getMyAudioBook(position).getDurationAt());
        intent.putExtra(Constants.INTENT_MY_AUDIO_BOOK_ID, adapter.getMyAudioBook(position).getId());
        intent.putExtra(Constants.INTENT_MY_AUDIO_BOOK_TIMESTAMP, adapter.getMyAudioBook(position).getLastPlayingTimestamp());
        startActivity(intent);
    }

    @Override
    public void onItemLongClick(int position) {
        new AlertDialogs(this).showDeleteAudioBookDialog(adapter.getMyAudioBook(position).getAudioBook(), v -> {
            DatabaseRepository.getInstance(getApplicationContext()).delete(adapter.getMyAudioBook(position));
            new Handler().postDelayed(this::getMyBooks, 500);

        });
    }
}