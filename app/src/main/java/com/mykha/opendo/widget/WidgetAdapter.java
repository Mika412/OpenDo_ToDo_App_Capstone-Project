package com.mykha.opendo.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mykha.opendo.Const;
import com.mykha.opendo.R;
import com.mykha.opendo.objects.ListTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;

public class WidgetAdapter implements RemoteViewsService.RemoteViewsFactory {
    String TAG = "WidgetAdapter";

    private List<ListTask> listTasks = new ArrayList<>();
    private Context context=null;
    private int appWidgetId;
    private FirebaseAuth auth;
    private String listId;
    private String listName;

    FirebaseDatabase database;

    WidgetAdapter(Context context, Intent intent) {
        this.context=context;
        appWidgetId=intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);

        SharedPreferences prefs = context.getSharedPreferences(Const.PREFS_NAME, MODE_PRIVATE);
        listId = prefs.getString(Const.WidgetPrefs + appWidgetId, "");
        listName = prefs.getString(Const.WidgetNamePrefs + appWidgetId, "");


        database = FirebaseDatabase.getInstance();

        auth = FirebaseAuth.getInstance();


    }

    @Override
    public void onCreate() {
        loadData();
    }


    @Override
    public void onDestroy() {
        // no-op
    }

    @Override
    public int getCount() {
        return(listTasks.size());
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews row=new RemoteViews(context.getPackageName(), R.layout.widget_item);
        row.setImageViewResource(R.id.radio_button_image, R.drawable.ic_radio_button);
        row.setTextViewText(R.id.itemName, listTasks.get(position).getName());

        Intent todoIntent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putString("listKey", listId);
        bundle.putString("listName", listName);
        bundle.putString("taskId", listTasks.get(position).getDocId());
        todoIntent.putExtras(bundle);
        todoIntent.setAction(WidgetProvider.OPEN_TASK_ACTION);
        row.setOnClickFillInIntent(R.id.itemName, todoIntent);

        Intent fillInIntent = new Intent();
        fillInIntent.setAction(WidgetProvider.CHECK_TASK_ACTION);
        fillInIntent.putExtras(bundle);
        row.setOnClickFillInIntent(R.id.radio_button_image, fillInIntent);
        return(row);
    }

    protected PendingIntent getPendingSelfIntent(Context context, String action) {
        Intent intent = new Intent(context, WidgetProvider.class);
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    @Override
    public RemoteViews getLoadingView() {
        return(null);
    }

    @Override
    public int getViewTypeCount() {
        return(1);
    }

    @Override
    public long getItemId(int position) {
        return(position);
    }

    @Override
    public boolean hasStableIds() {
        return(true);
    }

    @Override
    public void onDataSetChanged() {
        // no-op
    }

    private void loadData() {
        Log.e(TAG, "loadData: Entered LoadData");
        if (auth.getCurrentUser() == null) {
            AppWidgetManager mgr = AppWidgetManager.getInstance(context);
            ComponentName cn = new ComponentName(context, WidgetProvider.class);
            mgr.notifyAppWidgetViewDataChanged(mgr.getAppWidgetIds(cn), R.id.words);
            return;
        }


        DatabaseReference myRef = database.getReference("users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).child("lists").child(listId).child("tasks");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listTasks.clear();
                listTasks = new ArrayList<>();
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    Log.e(TAG, "onDataChange: blah " + postSnapshot.getKey());
                    Boolean isChecked;
                    if(postSnapshot.child("complete").getValue() == null)
                        isChecked = false;
                    else if(postSnapshot.child("complete").getValue().toString().equals("1"))
                        isChecked = true;
                    else
                        isChecked = false;
                    ListTask listTask = new ListTask(postSnapshot.getKey().toString(), 2, postSnapshot.child("name").getValue().toString(), isChecked);
                    if(!isChecked)
                        listTasks.add(listTask);


                }
                AppWidgetManager mgr = AppWidgetManager.getInstance(context);
                ComponentName cn = new ComponentName(context, WidgetProvider.class);
                mgr.notifyAppWidgetViewDataChanged(mgr.getAppWidgetIds(cn), R.id.words);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        });
    }
}
