package com.application.soundsaga.common.util;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.ObjectKey;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class Util {

    public static boolean isNetworkAvailable(Application application) {
        ConnectivityManager connectivityManager = (ConnectivityManager) application.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network nw = connectivityManager.getActiveNetwork();
        if (nw == null) return false;
        NetworkCapabilities actNw = connectivityManager.getNetworkCapabilities(nw);
        return actNw != null && (actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) || actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH));

    }

    public static String humanizeMilliseconds(long milliseconds) {
        int seconds = (int) (milliseconds / 1000) % 60;
        int minutes = (int) ((milliseconds / (1000 * 60)) % 60);
        int hours = (int) ((milliseconds / (1000 * 60 * 60)) % 24);
        return getTimeFromInt(hours) + ":" + getTimeFromInt(minutes) + ":" + getTimeFromInt(seconds);
    }

    public static String humanizeMillisecondsHoursExcluded(long milliseconds) {
        int seconds = (int) (milliseconds / 1000) % 60;
        int minutes = (int) ((milliseconds / (1000 * 60)) % 60);

        return getTimeFromInt(minutes) + ":" + getTimeFromInt(seconds);
    }

    public static String humanizeMillisecondsToDate(long milliseconds) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliseconds);

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);


        return getTimeFromInt(day) + "/" + getTimeFromInt(month) + "/" + getTimeFromInt(year) + "  " + getTimeFromInt(hour) + ":" + getTimeFromInt(minute);
    }

    private static String getTimeFromInt(int time) {
        if (String.valueOf(time).length() == 1) {
            return "0" + time;

        }
        return String.valueOf(time);
    }

    public static void loadImage(String url, ImageView imageView) {
        Glide.with(imageView.getContext()).load(url).centerCrop().into(imageView);
    }

    public static SpannableString makeTextWithUnderline(String text) {
        SpannableString content = new SpannableString(text);
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        return content;
    }


}
