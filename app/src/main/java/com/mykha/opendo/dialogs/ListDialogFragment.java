package com.mykha.opendo.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mykha.opendo.MainActivity;
import com.mykha.opendo.R;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ListDialogFragment extends DialogFragment {
    private final String TAG = MainActivity.class.getSimpleName();
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(getContext());
        View mView = layoutInflaterAndroid.inflate(R.layout.new_list_input_dialog_box, null);
        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(getContext());
        alertDialogBuilderUserInput.setView(mView);

        final EditText userInputDialogEditText =  mView.findViewById(R.id.userInputDialog);
        TextView titleView = mView.findViewById(R.id.dialogTitle);
        titleView.setText(R.string.new_list_title);
        alertDialogBuilderUserInput
                .setCancelable(false)
                .setPositiveButton(R.string.positiveDialogButton, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogBox, int id) {
                        String listName = userInputDialogEditText.getText().toString();
                        if(listName.isEmpty())
                            Toast.makeText(getActivity(), R.string.list_name_empty_alert, Toast.LENGTH_LONG).show();
                        else
                            addNewList(listName);

                    }
                })

                .setNegativeButton(R.string.negativeDialogButton,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogBox, int id) {
                                dialogBox.cancel();
                            }
                        });


        return alertDialogBuilderUserInput.create();
    }


    private void addNewList(final String listName) {
//        Log.d(TAG, "loadData: id is " + FirebaseAuth.getInstance().getUid());

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        Map<String, String> data = new HashMap<>();
        data.put("name", listName);


        DatabaseReference myRef = database.getReference("users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).child("lists").push();
        myRef.setValue(data);

    }
}
