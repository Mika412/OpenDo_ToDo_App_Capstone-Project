package com.mykha.opendo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mykha.opendo.adapters.ListTaskAdapter;
import com.mykha.opendo.dialogs.TaskDialogFragment;
import com.mykha.opendo.interfaces.MainSendDataInterface;
import com.mykha.opendo.objects.ListTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MainActivityFragment extends Fragment implements ListTaskAdapter.ToDoTaskClickListener{

    private static final String TAG = MainActivity.class.getSimpleName();
    private RecyclerView recyclerView;
    private List<ListTask> cartList;
    private ListTaskAdapter mAdapter;
    private CoordinatorLayout coordinatorLayout;
    private ProgressBar progressBar;
    private TextView no_tasksTv;
    private FloatingActionButton fabAdd;

    private String listId;
    private String listName;

    Activity mainActivity;

    MainSendDataInterface mCallback;

    public static MenuItem deleteItem;


    FirebaseDatabase database;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View inflated = inflater.inflate(R.layout.fragment_main, container, false);
        setHasOptionsMenu(true);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            listId = bundle.getString("listKey", "default");
            listName = bundle.getString("listName", "default");
        }

        recyclerView = inflated.findViewById(R.id.recycler_view);
        no_tasksTv = inflated.findViewById(R.id.no_tasksTv);
        coordinatorLayout = inflated.findViewById(R.id.coordinator_layout);
        progressBar = inflated.findViewById(R.id.loadingTasksProgressBar);
        fabAdd = inflated.findViewById(R.id.fabAddTask);
        cartList = new ArrayList<>();
        mAdapter = new ListTaskAdapter(getContext(), cartList, this);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(mAdapter);

        database = FirebaseDatabase.getInstance();


        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e(TAG, "onClick: activity " + (getActivity() == null) );
                showEditDialog();
            }
        });

        return inflated;
    }


    private void showEditDialog() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.addToBackStack(null);

        //Pass list name
        Bundle args = new Bundle();
        args.putString("listId", listId);

        DialogFragment dialogFragment = new TaskDialogFragment();
        dialogFragment.setArguments(args);
        dialogFragment.show(ft, "ListDialog");
    }

    @Override
    public void onStart() {
        super.onStart();
        if(listId == null || listId.isEmpty()){
            queryFirstList();
        }else {
           getData();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);


        if (context instanceof Activity){
            mainActivity=(MainActivity) context;

            mCallback = (MainSendDataInterface) mainActivity;
        }
    }

    private void queryFirstList() {

        DatabaseReference myRef = database.getReference("users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).child("lists");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.getChildrenCount() > 0){
                    Iterator iter = dataSnapshot.getChildren().iterator();
                    DataSnapshot data = (DataSnapshot) iter.next();
                    listId = data.getKey();
                    mCallback.sendListId(listId);
                    listName = data.child("name").getValue().toString();
                    ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(listName);
                    getData();
                }else{
                    noListsAvailable();
                    ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("");
                    deleteItem.setVisible(false);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Do something that differs the Activity's menu here
        super.onCreateOptionsMenu(menu, inflater);
        deleteItem = menu.findItem(R.id.delete_list);
    }

    @Override
    public void onClick(View view, ListTask position) {

        Log.e(TAG, "onClick: das das ");
        Intent intent = new Intent(getContext(), TodoDetailsActivity.class);

        Bundle bundle = new Bundle();
        bundle.putString("listKey", listId);
        bundle.putString("listName", listName);
        bundle.putString("taskId", position.getDocId());
        intent.putExtras(bundle);
        startActivity(intent);

    }

    private void getData(){
        DatabaseReference myRef = database.getReference("users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).child("lists").child(listId).child("tasks");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                cartList.clear();
                progressBar.setVisibility(View.INVISIBLE);
                no_tasksTv.setVisibility(View.INVISIBLE);
                fabAdd.show();
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
                        cartList.add(listTask);
                }

                if(cartList.isEmpty()) {
                    noTasksAvailable();
                }
                Log.d(TAG, "onComplete: is " + cartList.size());
                mAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        });
    }

    @Override
    public void onChecked(ListTask position) {
        cartList.remove(position);
//        mAdapter.removeItem(position);
        mAdapter.notifyDataSetChanged();

        Map<String, Object> checked = new HashMap<>();
        checked.put("complete", "1");
        DatabaseReference myRef = database.getReference("users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).child("lists").child(listId).child("tasks").child(position.getDocId());
        myRef.updateChildren(checked);
    }

    private void noTasksAvailable(){
        progressBar.setVisibility(View.INVISIBLE);
        no_tasksTv.setText(R.string.no_tasks);
        no_tasksTv.setVisibility(View.VISIBLE);

    }

    private void noListsAvailable() {
        progressBar.setVisibility(View.INVISIBLE);
        fabAdd.hide();
        no_tasksTv.setText(R.string.no_lists_available);
        no_tasksTv.setVisibility(View.VISIBLE);
    }
}
