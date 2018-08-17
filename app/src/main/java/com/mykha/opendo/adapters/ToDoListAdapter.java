package com.mykha.opendo.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mykha.opendo.R;
import com.mykha.opendo.objects.ToDoList;

import java.util.List;

public class ToDoListAdapter extends RecyclerView.Adapter<ToDoListAdapter.MyViewHolder> {
    private Context context;
    private List<ToDoList> toDoList;
    ToDoListAdapterClickListener listAdapterClickListener;
    private boolean isWidgetAdapter = false;

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView name, description;
        private ToDoListAdapterClickListener mListener;
        public MyViewHolder(View view, ToDoListAdapterClickListener listener) {
            super(view);
            name = view.findViewById(R.id.name);
            mListener = listener;
            view.setOnClickListener(this);
        }
        @Override
        public void onClick(View view) {
            Log.e("das", "onClick: " + getAdapterPosition() + " " + toDoList.size());
            mListener.onClick(view, toDoList.get(getAdapterPosition()));
        }
    }


    public ToDoListAdapter(Context context, List<ToDoList> cartList, ToDoListAdapterClickListener listener) {
        this.context = context;
        this.toDoList = cartList;
        listAdapterClickListener = listener;
    }
    public ToDoListAdapter(Context context, List<ToDoList> cartList, ToDoListAdapterClickListener listener, Boolean isWidgetAdapter) {
        this.context = context;
        this.toDoList = cartList;
        listAdapterClickListener = listener;
        this.isWidgetAdapter = isWidgetAdapter;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = null;
        if(isWidgetAdapter){
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.tasklist_item, parent, false);
        }else{
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.tasklist_widget_item, parent, false);
        }

        return new MyViewHolder(itemView, listAdapterClickListener);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        final ToDoList item = toDoList.get(position);
        holder.name.setText(item.getName());
    }

    @Override
    public int getItemCount() {
        return toDoList.size();
    }
    public void switchData(List<ToDoList> cartList){
        this.toDoList = cartList;
    }
    public interface ToDoListAdapterClickListener {
        void onClick(View view, ToDoList position);
    }
}