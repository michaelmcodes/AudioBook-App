package com.application.soundsaga.common.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.text.Html;
import android.view.View;

import androidx.appcompat.app.AlertDialog;

import com.application.soundsaga.R;
import com.application.soundsaga.common.data.model.AudioBook;
import com.application.soundsaga.common.util.Util;
import com.application.soundsaga.databinding.DialogAudioBookDetailsBinding;
import com.application.soundsaga.databinding.DialogDeleteAudioBookBinding;

public class AlertDialogs {
    AlertDialog.Builder alert;
    private Activity activity;

    public AlertDialogs(Activity activity) {
        alert = new AlertDialog.Builder(activity);
        this.activity = activity;
    }

    public void showBasicDialog(String title, String message, int drawableId, int positiveButtonId, Dialog.OnClickListener listener) {
        alert.setTitle(title);
        alert.setMessage(message);
        alert.setIcon(drawableId);
        alert.setPositiveButton(positiveButtonId, listener);
        AlertDialog dialog = alert.create();
        dialog.show();
    }

    public void showBasicDialog(String title, String message) {
        alert.setTitle(title);
        alert.setMessage(message);
        alert.setPositiveButton(R.string.ok, (dialog, which) -> {
        });
        AlertDialog dialog = alert.create();
        dialog.show();
    }

    public void showBasicDialog(String title, String message, DialogInterface.OnClickListener listener) {
        alert.setTitle(title);
        alert.setMessage(message);
        alert.setPositiveButton(R.string.ok, listener);
        AlertDialog dialog = alert.create();
        dialog.show();
    }

    public void showNoInternetDialog() {
        showBasicDialog(activity.getString(R.string.no_internet), activity.getString(R.string.please_check_your_internet_connection), (dialog, which) -> activity.finish());
    }

    public void showAudioBookDialogDetails(AudioBook audioBook) {
        DialogAudioBookDetailsBinding dialogAudioBookDetailsBinding = DialogAudioBookDetailsBinding.inflate(activity.getLayoutInflater());
        View dialogView = dialogAudioBookDetailsBinding.getRoot();

        Util.loadImage(audioBook.getImage(), dialogAudioBookDetailsBinding.ivAudioBookImage);
        dialogAudioBookDetailsBinding.tvAudioBookTitle.setText(audioBook.getTitle());
        dialogAudioBookDetailsBinding.tvAudioBookAuthor.setText(audioBook.getAuthor());
        dialogAudioBookDetailsBinding.tvNumberOfChapters.setText(audioBook.getContents().size() + " " + activity.getString(R.string.chapters));
        dialogAudioBookDetailsBinding.tvDuration.setText(activity.getString(R.string.duration) + " " + audioBook.getDuration());
        dialogAudioBookDetailsBinding.tvLanguage.setText(activity.getString(R.string.language) + " " + audioBook.getLanguage());
        alert.setView(dialogView);

        AlertDialog dialog = alert.create();

        dialogAudioBookDetailsBinding.tvOkButton.setOnClickListener(v -> dialog.dismiss());


        dialog.show();

    }

    public void showDeleteAudioBookDialog(AudioBook audioBook, View.OnClickListener onClickListener) {
        DialogDeleteAudioBookBinding dialogDeleteAudioBookBinding = DialogDeleteAudioBookBinding.inflate(activity.getLayoutInflater());
        View dialogView = dialogDeleteAudioBookBinding.getRoot();

        Util.loadImage(audioBook.getImage(), dialogDeleteAudioBookBinding.ivAudioBookImage);
        String message = activity.getString(R.string.remove_the_book_history) + " " + "<b>" + "<i>" + audioBook.getTitle() + "</b>" + "</i>" + "?";
        dialogDeleteAudioBookBinding.tvDialogMessage.setText(Html.fromHtml(message, Html.FROM_HTML_MODE_COMPACT));
        alert.setView(dialogView);

        AlertDialog dialog = alert.create();

        dialogDeleteAudioBookBinding.tvOkButton.setOnClickListener(v -> {
            onClickListener.onClick(v);
            dialog.dismiss();
        });
        dialogDeleteAudioBookBinding.tvCancelButton.setOnClickListener(v -> dialog.dismiss());


        dialog.show();

    }
}
