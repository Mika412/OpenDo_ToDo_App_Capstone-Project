package com.mykha.opendo;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class BackgroundJobService extends JobService {
    @Override
    public boolean onStartJob(JobParameters job) {
        Log.e("das", "onStartJob: Keep the firebase connection synced");
        //Keep database in sync
        DatabaseReference scoresRef = FirebaseDatabase.getInstance().getReference("users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()));
        scoresRef.keepSynced(true);

        //Check if user was deleted
        DatabaseReference deletedUser = FirebaseDatabase.getInstance().getReference("users");
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild(FirebaseAuth.getInstance().getUid())){    //Delete data

                    SharedPreferences preferences = getSharedPreferences(Const.PREFS_NAME, MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.clear();
                    editor.commit();

                    FirebaseAuth.getInstance().signOut();
                    Log.e("das", "onStartJob: User got deleted");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

        };
        deletedUser.addListenerForSingleValueEvent(valueEventListener);
        deletedUser.removeEventListener(valueEventListener);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        return true; // Answers the question: "Should this job be retried?"
    }
}