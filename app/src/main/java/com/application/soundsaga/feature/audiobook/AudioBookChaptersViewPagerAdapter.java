package com.application.soundsaga.feature.audiobook;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.application.soundsaga.common.data.model.AudioBook;
import com.application.soundsaga.common.data.model.Chapter;
import com.application.soundsaga.common.util.Util;
import com.application.soundsaga.databinding.LayoutAudioBookChaptersViewPagerBinding;

public class AudioBookChaptersViewPagerAdapter extends RecyclerView.Adapter<AudioBookChaptersViewPagerAdapter.MyViewHolder> {

    private AudioBook audioBook;

    public void setData(AudioBook audioBook) {
        this.audioBook = audioBook;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutAudioBookChaptersViewPagerBinding binding = LayoutAudioBookChaptersViewPagerBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new MyViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.bind(audioBook.getContents().get(position), audioBook);
    }

    @Override
    public int getItemCount() {
        return audioBook.getContents().size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        private final LayoutAudioBookChaptersViewPagerBinding binding;

        public MyViewHolder(@NonNull LayoutAudioBookChaptersViewPagerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }


        public void bind(Chapter chapter, AudioBook audioBook) {
            Util.loadImage(audioBook.getImage(), binding.ivAudioBookImage);
            String title = chapter.getTitle() + "(" + (getAdapterPosition() + 1) + " of " + audioBook.getContents().size() + ")";
            binding.tvChapterTitle.setText(title);
            binding.tvChapterTitle.setSelected(true);
        }
    }
}
