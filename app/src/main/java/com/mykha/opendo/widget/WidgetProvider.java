package com.mykha.opendo.widget;

import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mykha.opendo.Const;
import com.mykha.opendo.MainActivity;
import com.mykha.opendo.R;
import com.mykha.opendo.TodoDetailsActivity;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class WidgetProvider extends AppWidgetProvider {
    private static final String TAG = "WidgetProvider";
    public static final String OPEN_TASK_ACTION = "OPEN_TASK_ACTION";
    public static final String CHECK_TASK_ACTION = "CHECK_TASK_ACTION";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        Log.e(TAG, "onUpdate: entered");
        for (int appWidgetId : appWidgetIds) {
            RemoteViews remoteViews = updateAppWidget(context, appWidgetId);
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    static RemoteViews updateAppWidget(Context context, int appWidgetId) {

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(),R.layout.widget);

        Intent svcIntent = new Intent(context, WidgetService.class);
        svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        remoteViews.setRemoteAdapter(R.id.words, svcIntent);
        remoteViews.setEmptyView(R.id.words, R.id.empty_view);


        //Check if logged in

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
//            remoteViews.setViewVisibility(R.id.words, );
//            no_listsTV.setText(R.string.login_first);
        }


        SharedPreferences prefs = context.getSharedPreferences(Const.PREFS_NAME, Context.MODE_PRIVATE);
        Log.e(TAG, "updateAppWidget: " + prefs.getString(Const.WidgetPrefs + appWidgetId,"Default"));
        //intent for App Start Button
        Intent intentStart = new Intent(context, MainActivity.class);
        if(!prefs.getString(Const.WidgetPrefs + appWidgetId,"Default").equals("Default")) {
            Bundle bundle = new Bundle();
            bundle.putString("listKey", prefs.getString(Const.WidgetPrefs + appWidgetId, "Default"));
            bundle.putString("name", prefs.getString(Const.WidgetNamePrefs + appWidgetId, "Default"));
            Log.e(TAG, "showAppWidget: widget id after " + appWidgetId);
            intentStart.putExtras(bundle);
        }
        PendingIntent pendingIntentstart = PendingIntent.getActivity(context, 0, intentStart, PendingIntent.FLAG_CANCEL_CURRENT);
        remoteViews.setTextViewText(R.id.list_title, prefs.getString(Const.WidgetNamePrefs + appWidgetId, "Default"));
        remoteViews.setOnClickPendingIntent(R.id.list_title, pendingIntentstart);


        Intent toastIntent = new Intent(context, WidgetProvider.class);
        toastIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent toastPendingIntent = PendingIntent.getBroadcast(context, 0, toastIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setPendingIntentTemplate(R.id.words, toastPendingIntent);
        return remoteViews;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(OPEN_TASK_ACTION)) {
//            Toast.makeText(context, "Opened task", Toast.LENGTH_SHORT).show();
            Intent descriptionIntent = new Intent(context, TodoDetailsActivity.class);
            descriptionIntent.putExtras(intent.getExtras());
            context.startActivity(descriptionIntent);

        }else if (intent.getAction().equals(CHECK_TASK_ACTION)) {
//            Toast.makeText(context, "Checked", Toast.LENGTH_SHORT).show();
            Bundle bundle = intent.getExtras();

            FirebaseDatabase database = FirebaseDatabase.getInstance();

            Map<String, Object> checked = new HashMap<>();
            checked.put("complete", "1");
            DatabaseReference myRef = database.getReference("users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).child("lists").child(bundle.getString("listKey")).child("tasks").child(bundle.getString("taskId"));
            myRef.updateChildren(checked);
        }
        super.onReceive(context, intent);
    }
}