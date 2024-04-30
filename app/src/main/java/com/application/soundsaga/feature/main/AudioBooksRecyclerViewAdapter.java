package com.application.soundsaga.feature.main;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.application.soundsaga.common.data.model.AudioBook;
import com.application.soundsaga.common.util.Util;
import com.application.soundsaga.databinding.LayoutAudioBooksRecyclerViewBinding;

import java.util.ArrayList;
import java.util.List;

public class AudioBooksRecyclerViewAdapter extends RecyclerView.Adapter<AudioBooksRecyclerViewAdapter.MyViewHolder> {

    private List<AudioBook> audioBooks = new ArrayList<>();

    public void setData(List<AudioBook> audioBooks) {
        this.audioBooks = audioBooks;
        notifyDataSetChanged();
    }

    public AudioBook getAudioBook(int position) {
        return audioBooks.get(position);
    }


    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    private OnItemLongClickListener onItemLongClickListener;

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }


    public interface OnItemClickListener {
        void onItemClick(int position);
    }


    public interface OnItemLongClickListener {
        void onItemLongClick(int position);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutAudioBooksRecyclerViewBinding binding = LayoutAudioBooksRecyclerViewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new MyViewHolder(binding, onItemClickListener, onItemLongClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        AudioBook audioBook = audioBooks.get(position);
        holder.bind(audioBook);
    }

    @Override
    public int getItemCount() {
        return audioBooks.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        private final LayoutAudioBooksRecyclerViewBinding binding;

        public MyViewHolder(@NonNull LayoutAudioBooksRecyclerViewBinding binding,
                            OnItemClickListener onItemClickListener,
                            OnItemLongClickListener onItemLongClickListener) {
            super(binding.getRoot());
            this.binding = binding;


            if (onItemClickListener != null)
                itemView.setOnClickListener(v -> onItemClickListener.onItemClick(getAdapterPosition()));

            if (onItemLongClickListener != null)
                itemView.setOnLongClickListener(v -> {
                    onItemLongClickListener.onItemLongClick(getAdapterPosition());
                    return true;
                });
        }


        public void bind(AudioBook audioBook) {
            Util.loadImage(audioBook.getImage(), binding.ivAudioBookImage);
            binding.tvAudioBookTitle.setText(audioBook.getTitle());
            binding.tvAudioBookTitle.setSelected(true);
            binding.tvAudioBookAuthor.setText(audioBook.getAuthor());
            binding.tvAudioBookAuthor.setSelected(true);
        }
    }
}
