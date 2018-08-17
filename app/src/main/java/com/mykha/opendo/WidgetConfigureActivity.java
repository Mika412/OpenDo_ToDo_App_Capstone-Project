package com.mykha.opendo;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mykha.opendo.adapters.ToDoListAdapter;
import com.mykha.opendo.objects.ToDoList;
import com.mykha.opendo.widget.WidgetProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WidgetConfigureActivity extends AppCompatActivity implements ToDoListAdapter.ToDoListAdapterClickListener{
    private static final String TAG = "WidgetConfigureActivity";

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    FirebaseDatabase database;
    private List<ToDoList> toDoLists;
    private ToDoListAdapter mAdapter;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.no_listsTV)
    TextView no_listsTV;


    private FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_widget_configuration);
        ButterKnife.bind(this);

        setResult(RESULT_CANCELED);

        toolbar.setTitle(R.string.chooseList);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);

        toDoLists = new ArrayList<>();
        mAdapter = new ToDoListAdapter(getApplicationContext(), toDoLists, this);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(mAdapter);


        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();


        if (auth.getCurrentUser() != null) {
            loadData();
        }else{
            no_listsTV.setVisibility(View.VISIBLE);
            no_listsTV.setText(R.string.login_first);
        }
    }

    private void loadData() {
        DatabaseReference myRef = database.getReference("users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).child("lists");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                toDoLists.clear();
                toDoLists = new ArrayList<>();
                if(dataSnapshot.getChildrenCount() > 0) {
                    for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                        // TODO: handle the post
                        Log.e(TAG, "onDataChange: value is " + postSnapshot.getKey() + " " + postSnapshot.child("name").getValue());
                        toDoLists.add(new ToDoList(postSnapshot.getKey().toString(), 0, Objects.requireNonNull(postSnapshot.child("name").getValue()).toString()));
                    }
                    mAdapter.switchData(toDoLists);
                    mAdapter.notifyDataSetChanged();
                }else{
                    no_listsTV.setVisibility(View.VISIBLE);
                    no_listsTV.setText(R.string.no_lists_available);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        });
    }



    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private void showAppWidget(ToDoList listId) {
        Intent configIntent = getIntent();
        Bundle extras = configIntent.getExtras();
        if(extras != null){
            mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }
        if(mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID){
            finish();
        }

        SharedPreferences prefs = getSharedPreferences(Const.PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Const.WidgetPrefs + mAppWidgetId, listId.getDocId());
        editor.putString(Const.WidgetNamePrefs + mAppWidgetId, listId.getName());
        editor.apply();

        Log.e(TAG, "showAppWidget: widget id configure " + mAppWidgetId);
        Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE, null, this, WidgetProvider.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[] {mAppWidgetId});
        sendBroadcast(intent);

        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                mAppWidgetId);
        setResult(RESULT_OK, resultValue);
        finish();
    }


    @Override
    public void onClick(View view, ToDoList position) {
        showAppWidget(position);
    }
}