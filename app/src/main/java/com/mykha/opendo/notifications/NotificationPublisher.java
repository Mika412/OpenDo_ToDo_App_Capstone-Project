package com.mykha.opendo.notifications;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import com.mykha.opendo.Const;

import java.util.Random;

import static android.content.Context.MODE_PRIVATE;

public class NotificationPublisher extends BroadcastReceiver {

    int importance = NotificationManager.IMPORTANCE_HIGH;

    @TargetApi(Build.VERSION_CODES.O)
    public void onReceive(Context context, Intent intent) {

        NotificationChannel mChannel = new NotificationChannel(Const.notification_channel_id, Const.visible_otification_channel_id, importance);
        NotificationManager mNotificationManager =(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = intent.getParcelableExtra(Const.NOTIFICATION);
        int notificationId = intent.getIntExtra(Const.NOTIFICATION_ID, 0);
        String todoKey = intent.getStringExtra(Const.NOTIFICAITON_TASKKEY);
        mNotificationManager.createNotificationChannel(mChannel);

        SharedPreferences sharedPreferences = context.getSharedPreferences(Const.PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor sharedEditor = sharedPreferences.edit();
        sharedEditor.putBoolean(Const.ReminderDonePref + todoKey, true);
        Log.e("das", "Should delete pref" + Const.ReminderDonePref + todoKey);
        sharedEditor.apply();

        mNotificationManager.notify(notificationId, notification);
    }
}