package com.application.soundsaga.feature.mybooks;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.application.soundsaga.common.data.model.AudioBook;
import com.application.soundsaga.common.data.model.MyAudioBook;
import com.application.soundsaga.common.util.Util;
import com.application.soundsaga.databinding.LayoutAudioBooksRecyclerViewBinding;
import com.application.soundsaga.databinding.LayoutMyAudioBooksRecyclerViewBinding;

import java.util.ArrayList;
import java.util.List;

public class MyAudioBooksRecyclerViewAdapter extends RecyclerView.Adapter<MyAudioBooksRecyclerViewAdapter.MyViewHolder> {

    private List<MyAudioBook> myAudioBooks = new ArrayList<>();

    public void setData(List<MyAudioBook> myAudioBooks) {
        this.myAudioBooks = myAudioBooks;
        notifyDataSetChanged();
    }

    public MyAudioBook getMyAudioBook(int position) {
        return myAudioBooks.get(position);
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
        LayoutMyAudioBooksRecyclerViewBinding binding = LayoutMyAudioBooksRecyclerViewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new MyViewHolder(binding, onItemClickListener, onItemLongClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        MyAudioBook audioBook = myAudioBooks.get(position);
        holder.bind(audioBook);
    }

    @Override
    public int getItemCount() {
        return myAudioBooks.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        private final LayoutMyAudioBooksRecyclerViewBinding binding;

        public MyViewHolder(@NonNull LayoutMyAudioBooksRecyclerViewBinding binding,
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


        public void bind(MyAudioBook myAudioBook) {
            Util.loadImage(myAudioBook.getAudioBook().getImage(), binding.ivAudioBookImage);
            binding.tvAudioBookTitle.setText(myAudioBook.getAudioBook().getTitle());
            binding.tvAudioBookAuthor.setText(myAudioBook.getAudioBook().getAuthor());
            binding.tvAudioBookChapterTitle.setText(myAudioBook.getAudioBook().getContents().get(0).getTitle());
            binding.tvAudioBookTimestamp.setText(Util.humanizeMillisecondsToDate(myAudioBook.getLastPlayingTimestamp()));
            String durations = Util.humanizeMillisecondsHoursExcluded(myAudioBook.getDurationAt()) + " of " + Util.humanizeMillisecondsHoursExcluded(myAudioBook.getTotalDuration());
            binding.tvAudioBookChapterDurations.setText(durations);
        }
    }
}
