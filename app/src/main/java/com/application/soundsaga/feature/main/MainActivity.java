package com.application.soundsaga.feature.main;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;
import androidx.recyclerview.widget.GridLayoutManager;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.application.soundsaga.R;
import com.application.soundsaga.common.data.model.AudioBook;
import com.application.soundsaga.common.database.DatabaseRepository;
import com.application.soundsaga.common.dialog.AlertDialogs;
import com.application.soundsaga.common.util.Constants;
import com.application.soundsaga.common.util.Util;
import com.application.soundsaga.databinding.ActivityMainBinding;
import com.application.soundsaga.feature.audiobook.AudioBookActivity;
import com.application.soundsaga.feature.audiobook.MediaPlayerService;
import com.application.soundsaga.feature.mybooks.MyBooksActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AudioBooksRecyclerViewAdapter.OnItemLongClickListener, AudioBooksRecyclerViewAdapter.OnItemClickListener {

    private ActivityMainBinding binding;

    private final AudioBooksRecyclerViewAdapter adapter = new AudioBooksRecyclerViewAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initViews();

        if (Util.isNetworkAvailable(getApplication())) {
            getAudioBooks();
        } else {
            new AlertDialogs(this).showNoInternetDialog();
        }

        binding.ivBooksMenu.setOnClickListener(v -> new Thread(() -> {
            int myBooksSize = DatabaseRepository.getInstance(getApplicationContext()).getAll().size();

            runOnUiThread(() -> {
                if (myBooksSize > 0) {
                    startActivity(new Intent(getApplicationContext(), MyBooksActivity.class));
                } else {
                    new AlertDialogs(this).showBasicDialog(getString(R.string.my_books_shelf_empty), getString(R.string.you_have_no_audio_books), R.drawable.logo, R.string.ok, (dialog, which) -> {

                    });
                }

            });
        }).start());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission();
        }
    }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private void requestNotificationPermission() {
        if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
        }
    }


    private void initViews() {
        int orientation = getResources().getConfiguration().orientation;
        int gridCount = 2;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) gridCount = 4;
        binding.rvAudioBooks.setAdapter(adapter);
        binding.rvAudioBooks.setLayoutManager(new GridLayoutManager(getApplicationContext(), gridCount));

        adapter.setOnItemClickListener(this);
        adapter.setOnItemLongClickListener(this);
    }

    private void getAudioBooks() {
        RequestQueue queue = Volley.newRequestQueue(this);

        String url = "https://christopherhield.com/ABooks/abook_contents.json";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, response -> {
            Type listType = new TypeToken<List<AudioBook>>() {
            }.getType();
            List<AudioBook> audioBooks = new Gson().fromJson(response, listType);

            runOnUiThread(() -> {
                adapter.setData(audioBooks);
            });


        }, error -> {
            if (error.getLocalizedMessage() != null) {
                Log.e("volley error", error.getLocalizedMessage());
                runOnUiThread(() -> {
                    Toast.makeText(getApplicationContext(), error.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });

        queue.add(stringRequest);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MediaPlayerService.mediaPlayerService.close();
    }

    @Override
    public void onItemClick(int position) {
        Intent audioBookIntent = new Intent(MainActivity.this, AudioBookActivity.class);
        audioBookIntent.putExtra(Constants.INTENT_AUDIO_BOOK, new Gson().toJson(adapter.getAudioBook(position)));
        startActivity(audioBookIntent);
    }

    @Override
    public void onItemLongClick(int position) {
        new AlertDialogs(this).showAudioBookDialogDetails(adapter.getAudioBook(position));
    }
}