package com.mykha.opendo;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.mykha.opendo.notifications.NotificationPublisher;

import java.util.Calendar;
import java.util.Random;

public class NotificationUtil {
    static String TAG = "NotificationUtil";
    static Notification getNotification(Context context, Bundle bundle) {

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(Const.notification_channel_id, "My Notifications", NotificationManager.IMPORTANCE_HIGH);

            // Configure the notification channel.
            notificationChannel.setDescription("Channel description");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);
            assert notificationManager != null;
            notificationManager.createNotificationChannel(notificationChannel);
        }

        Intent intent = new Intent(context, TodoDetailsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);


        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);


        SharedPreferences preferences = context.getSharedPreferences(Const.PREFS_NAME, Context.MODE_PRIVATE);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, Const.notification_channel_id);

        Notification notification = notificationBuilder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
//                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_checkmark)
                .setTicker(context.getString(R.string.app_name))
                .setContentIntent(pendingIntent)
                .setContentTitle(context.getResources().getString(R.string.notificationTitle))
                .setContentText(bundle.getString("taskName"))
                .setContentInfo("Info").build();
        return notification;
    }

    static void scheduleToDoNotification(Context context, Notification notification, Calendar notificationTime, String taskKey, SharedPreferences.Editor sharedEditor) {
        Random random = new Random();
        int m = random.nextInt(9999 - 1000) + 1000;

        //Save the notification id in the shared preferences
        sharedEditor.putString(Const.NOTIFICATION_ID+taskKey, m+"");
        sharedEditor.apply();
        Log.e("das", "Should schedule with id " + m);
        Intent notificationIntent = new Intent(context, NotificationPublisher.class);
        notificationIntent.putExtra(Const.NOTIFICATION_ID, m);
        notificationIntent.putExtra(Const.NOTIFICATION, notification);
        notificationIntent.putExtra(Const.NOTIFICAITON_TASKKEY, taskKey);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, m, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        assert alarmManager != null;
        alarmManager.set(AlarmManager.RTC_WAKEUP, notificationTime.getTimeInMillis(), pendingIntent);
    }

    public static void deleteReminder(Context context, Notification notification, String taskKey, SharedPreferences sharedPreferences) {


        String notId = sharedPreferences.getString(Const.NOTIFICATION_ID+taskKey, "");
        int m = Integer.parseInt(notId.replace(Const.NOTIFICATION_ID, ""));
        Log.e(TAG, "deleteReminder: Supposed " + m + " " + notId);
        Intent notificationIntent = new Intent(context, NotificationPublisher.class);
        notificationIntent.putExtra(Const.NOTIFICATION_ID, m);
        notificationIntent.putExtra(Const.NOTIFICATION, notification);
        notificationIntent.putExtra(Const.NOTIFICAITON_TASKKEY, taskKey);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, m, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        assert alarmManager != null;
        alarmManager.cancel(pendingIntent);

        pendingIntent.cancel();
    }
}
