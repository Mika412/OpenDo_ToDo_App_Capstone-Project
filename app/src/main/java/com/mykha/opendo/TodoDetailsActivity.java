package com.mykha.opendo;

import android.annotation.SuppressLint;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

public class TodoDetailsActivity extends AppCompatActivity {

    private static final String TAG = TodoDetailsActivity.class.getSimpleName();

    private String listId;
    private String listName;
    private String taskId;

    private String nameTask = "";
    private String taskDescription = "";

    RadioGroup radioGroup;
    RadioButton radioButton;
    EditText editTextName;
    EditText editTextDescription;
    TextView reminderText;
    TextView timeAgo;
    ImageView imageView;
    ImageView deleteReminderImageView;
    FirebaseDatabase database;

    RelativeLayout reminderParent;

    SharedPreferences preferences;
    SharedPreferences.Editor sharedEditor;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        preferences = getSharedPreferences(Const.PREFS_NAME, MODE_PRIVATE);
        sharedEditor = preferences.edit();

        boolean useDarkTheme = preferences.getBoolean(Const.PREF_DARK_THEME, false);

        if (useDarkTheme) {
            setTheme(R.style.AppTheme_Dark);
        }
        if (getIntent().getExtras() != null) {
            Bundle extras = getIntent().getExtras();
            listId = extras.getString("listKey");
            taskId = extras.getString("taskId");
            listName = extras.getString("listName");
        } else {
            finish();
        }

        setContentView(R.layout.activity_todo_details);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        getSupportActionBar().setTitle(listName);

        radioGroup = findViewById(R.id.radio_group_title);
        radioButton = findViewById(R.id.radio_button_title);
        editTextName = findViewById(R.id.task_name);
        editTextDescription = findViewById(R.id.note_EditText);
        timeAgo = findViewById(R.id.timeAgoTx);
        imageView = findViewById(R.id.delete_task);
        reminderParent = findViewById(R.id.reminderParent);
        reminderText = findViewById(R.id.reminderText);
        deleteReminderImageView = findViewById(R.id.deleteReminderImageView);

        database = FirebaseDatabase.getInstance();

        DatabaseReference myRef = database.getReference("users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).child("lists").child(listId).child("tasks").child(taskId);
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.e(TAG, "onDataChange: " + dataSnapshot.getKey());
                nameTask = dataSnapshot.child("name").getValue().toString();
                taskDescription = dataSnapshot.child("description").getValue().toString();


                long time = (long) dataSnapshot.child("timestamp").getValue();
                editTextName.setText(nameTask);
                editTextDescription.setText(taskDescription);
                timeAgo.setText(getTimeAgo(time/1000));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        });

        radioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                boolean isChecked = radioButton.isSelected();
                radioButton.setChecked(!isChecked);
                radioButton.setSelected(!isChecked);

                String checkedValue = "0";
                if(isChecked)
                    checkedValue = "1";

                Map<String, Object> checked = new HashMap<>();
                checked.put("complete", checkedValue);
                DatabaseReference myRef = database.getReference("users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).child("lists").child(listId).child("tasks").child(taskId);
                myRef.updateChildren(checked);

            }
        });

        editTextDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String changedText = editable.toString();


                Map<String, Object> checked = new HashMap<>();
                checked.put("description", changedText);
                DatabaseReference myRef = database.getReference("users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).child("lists").child(listId).child("tasks").child(taskId);
                myRef.updateChildren(checked);
//
            }
        });

        editTextName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String changedText = editable.toString();

                Map<String, Object> checked = new HashMap<>();
                checked.put("name", changedText);
                DatabaseReference myRef = database.getReference("users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).child("lists").child(listId).child("tasks").child(taskId);
                myRef.updateChildren(checked);
            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DatabaseReference myRef = database.getReference("users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).child("lists").child(listId).child("tasks").child(taskId);
                myRef.removeValue(new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        Log.d(TAG, "DocumentSnapshot successfully deleted!");
                        finish();
                    }
                });
            }
        });

        reminderParent.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                Log.e("dasd", "onClick: Should have scheduled a notification");

                if (PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean(Const.AllowedNoticifactionsPref, true)) {
                    String taskIdSaved = preferences.getString(Const.KeyHeaderPref + taskId, "");
                    if (!taskIdSaved.isEmpty()) {
                        NotificationUtil.deleteReminder(getApplicationContext(), NotificationUtil.getNotification(getApplicationContext(), getBundledData()), taskId, preferences);
                    }
                    timePicker();
                } else {
                    Snackbar mySnackbar = Snackbar.make(findViewById(R.id.content_todo_coordinatorLay),
                            R.string.not_allowed_notifications, Snackbar.LENGTH_SHORT);
                    mySnackbar.show();
                }
            }
        });

        Log.e("das", "Value pref" + preferences.getBoolean(Const.ReminderDonePref + taskId, false));
        if (preferences.getBoolean(Const.ReminderDonePref + taskId, false)) {
            deletePrefReminder();
        }

        //Check if alarm set
        String taskIdSaved = preferences.getString(Const.KeyHeaderPref + taskId, "");
        if (!taskIdSaved.isEmpty() && !taskIdSaved.equals("")) {
            Long timeSet = preferences.getLong(Const.ReminderTimePref + taskId, 0);
            Calendar rightNow = Calendar.getInstance();
            rightNow.setTimeInMillis(timeSet);

            reminderText.setText("Reminder set to " + rightNow.get(Calendar.HOUR_OF_DAY) + ":" + (rightNow.get(Calendar.MINUTE) < 10 ? "0" + rightNow.get(Calendar.MINUTE) : rightNow.get(Calendar.MINUTE)));
            deleteReminderImageView.setVisibility(View.VISIBLE);

        } else {
            deleteReminderImageView.setVisibility(View.INVISIBLE);
        }

        deleteReminderImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NotificationUtil.deleteReminder(getApplicationContext(), NotificationUtil.getNotification(getApplicationContext(), getBundledData()), taskId, preferences);
                reminderText.setText(R.string.remind_me_text);
                deleteReminderImageView.setVisibility(View.INVISIBLE);
                deletePrefReminder();
            }
        });
    }

    private void deletePrefReminder() {
        sharedEditor.remove(Const.ListIdHeaderPref + taskId);
        sharedEditor.remove(Const.ListNameHeaderPref + taskId);
        sharedEditor.remove(Const.KeyHeaderPref + taskId);
        sharedEditor.remove(Const.KeyNamePref + taskId);
        sharedEditor.remove(Const.ReminderTimePref + taskId);
        sharedEditor.remove(Const.ReminderDonePref + taskId);
        sharedEditor.apply();
    }

    public void timePicker() {
        Calendar mcurrentTime = Calendar.getInstance();
        int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mcurrentTime.get(Calendar.MINUTE);
        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {

                Calendar calNow = Calendar.getInstance();
                Calendar calSet = (Calendar) calNow.clone();
                calSet.set(Calendar.HOUR_OF_DAY, selectedHour);
                calSet.set(Calendar.MINUTE, selectedMinute);
                calSet.set(Calendar.SECOND, 0);
                calSet.set(Calendar.MILLISECOND, 0);
                if (calSet.compareTo(calNow) <= 0) {
                    calSet.add(Calendar.DATE, 1);
                }

                sharedEditor.putString(Const.ListIdHeaderPref + taskId, listId);
                sharedEditor.putString(Const.ListNameHeaderPref + taskId, listName);
                sharedEditor.putString(Const.KeyHeaderPref + taskId, taskId);
                sharedEditor.putString(Const.KeyNamePref + taskId, nameTask);
                sharedEditor.putLong(Const.ReminderTimePref + taskId, calSet.getTimeInMillis());
                sharedEditor.commit();

                NotificationUtil.scheduleToDoNotification(getApplicationContext(), NotificationUtil.getNotification(getApplicationContext(), getBundledData()), calSet, taskId, sharedEditor);


                reminderText.setText("Reminder set to " + selectedHour + ":" + selectedMinute);
                deleteReminderImageView.setVisibility(View.VISIBLE);
                Log.e(TAG, selectedHour + ":" + selectedMinute);
            }
        }, hour, minute, true);
        mTimePicker.setTitle(R.string.selectTime);
        mTimePicker.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }


    Bundle getBundledData() {
        Bundle bundle = new Bundle();
        bundle.putString("listKey", listId);
        bundle.putString("listName", listName);
        bundle.putString("taskId", taskId);
        bundle.putString("taskName", nameTask);
        return bundle;
    }


    void getTimedTime(String timeRe) {
        Log.e("das", "getTimedTime: time is " + timeRe);
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        long time = 0;
        try {
            time = sdf.parse(timeRe).getTime();
//            time = sdf.parse("2016-01-24T16:00:00.000Z").getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long now = System.currentTimeMillis();

        CharSequence ago =
                DateUtils.getRelativeTimeSpanString(time, now, DateUtils.MINUTE_IN_MILLIS);
        Log.e(TAG, "getTimedTime: " + ago);
    }


    public static String getTimeAgo(long timestamp) {
        long currentSeconds = System.currentTimeMillis()/1000;

        long seconds = currentSeconds - timestamp;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        String timeString = "";
        if(seconds<60) {
            timeString = seconds+" seconds ago";
        }
        else if(minutes<60) {
            timeString = minutes+" minutes ago";
        }
        else if(hours<24) {
            timeString= hours+" hours ago";
        }
        else {
            timeString = days+" days ago";
        }
        return timeString;
    }
}
