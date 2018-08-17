package com.mykha.opendo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mykha.opendo.interfaces.MainSendDataInterface;
import com.mykha.opendo.objects.ToDoList;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity  implements MainSendDataInterface {

    private final String TAG = MainActivity.class.getSimpleName();

    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;

    private ActionBarDrawerToggle mDrawerToggle;

    @BindView(R.id.imageViewProfile)
    ImageView imageView;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    String currentList = "default";

    SharedPreferences preferences;

    FirebaseDatabase database;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = getSharedPreferences(Const.PREFS_NAME, MODE_PRIVATE);
        boolean useDarkTheme = preferences.getBoolean(Const.PREF_DARK_THEME, false);

        if(useDarkTheme) {
            setTheme(R.style.AppTheme_Dark);
        }

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);


//        toolbar = (Toolbar) findViewById(R.id.toolbar);

        MainActivityFragment fragment = new MainActivityFragment();

        Bundle bundle = getIntent().getExtras();
        if(bundle != null) {
            fragment.setArguments(bundle);
            toolbar.setTitle(bundle.getString("name"));
        }
        replaceFragment(fragment);


        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);

//        mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,toolbar ,  0, 0) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                //getActionBar().setTitle(mTitle);
                //invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                //getActionBar().setTitle(mDrawerTitle);
                //invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerToggle.syncState();
        mDrawerLayout.addDrawerListener(mDrawerToggle);

//        imageView = findViewById(R.id.imageViewProfile);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivityForResult(intent, 1);
            }
        });

        database = FirebaseDatabase.getInstance();

        //Start background sync job dispatcher

        //Keep database in sync
        DatabaseReference scoresRef = database.getReference("users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()));
        scoresRef.keepSynced(true);

        // Create a new dispatcher using the Google Play driver.
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(getApplicationContext()));

        Job myJob = dispatcher.newJobBuilder()
                // the JobService that will be called
                .setService(BackgroundJobService.class)
                // uniquely identifies the job
                .setTag("unique-tag65464")
                .setRecurring(true)
                .setLifetime(Lifetime.FOREVER)
                .setReplaceCurrent(true)
                .setConstraints(Constraint.ON_ANY_NETWORK)
                .setTrigger(Trigger.executionWindow(60, 300))
                .build();
        dispatcher.mustSchedule(myJob);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        boolean useDarkTheme = preferences.getBoolean(Const.PREF_DARK_THEME, false);

        changeTheme(useDarkTheme);
    }
    public void changeTheme(boolean useDarkTheme){
        if(useDarkTheme) {
            setTheme(R.style.AppTheme_Dark);
        }else {
            setTheme(R.style.AppTheme);
        }
        recreate();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.home){
            mDrawerLayout.openDrawer(GravityCompat.START);
        }else if (id == R.id.delete_list) {
            deleteList();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void deleteList() {

        DatabaseReference myRef = database.getReference("users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).child("lists").child(currentList);
        myRef.removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                Log.d(TAG, "List successfully deleted!");
                MainActivityFragment fragment = new MainActivityFragment();
                replaceFragment(fragment);
            }
        });
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    public void replaceFragment(Fragment fragment){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.contentFragment, fragment);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.addToBackStack(null);
        transaction.commit();
    }


    @Override
    public void sendList(ToDoList list) {
        mDrawerLayout.closeDrawer(Gravity.START, true);
        toolbar.setTitle(list.getName());

        currentList = list.getDocId();

        MainActivityFragment fragment = new MainActivityFragment();
        Bundle bundle = new Bundle();
        bundle.putString("listKey", list.getDocId());
        bundle.putString("listName", list.getName());
        fragment.setArguments(bundle);
        replaceFragment(fragment);
    }

    @Override
    public void sendListId(String listId) {
        currentList = listId;
    }
}
