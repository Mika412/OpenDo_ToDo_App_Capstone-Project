package com.mykha.opendo.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mykha.opendo.R;
import com.mykha.opendo.objects.ListTask;
import com.mykha.opendo.objects.ToDoList;

import java.util.List;

public class ListTaskAdapter extends RecyclerView.Adapter<ListTaskAdapter.MyViewHolder> {
    private Context context;
    private List<ListTask> taskList;
    ToDoTaskClickListener toDoTaskClickListener;
    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
        public TextView name, description;
        public RadioButton radioButton;
        private ToDoTaskClickListener mListener;
        public MyViewHolder(View view, ToDoTaskClickListener clickListener) {
            super(view);
            name = view.findViewById(R.id.name);
            radioButton = view.findViewById(R.id.radio_button);
            mListener = clickListener;
            view.setOnClickListener(this);
            radioButton.setOnCheckedChangeListener(this);
        }

        @Override
        public void onClick(View view) {
            Log.e("das", "onClick: " + getAdapterPosition() + " " + taskList.size());
            mListener.onClick(view, taskList.get(getAdapterPosition()));
        }

        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            radioButton.setChecked(false);
            mListener.onChecked(taskList.get(getAdapterPosition()));
        }
    }


    public ListTaskAdapter(Context context, List<ListTask> cartList, ToDoTaskClickListener clickListener) {
        this.context = context;
        this.taskList = cartList;
        toDoTaskClickListener = clickListener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.todo_item, parent, false);

        return new MyViewHolder(itemView, toDoTaskClickListener);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        final ListTask item = taskList.get(position);
        holder.name.setText(item.getName());
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }
    public void removeItem(ListTask position) {
        taskList.remove(position);
        notifyDataSetChanged();
    }
    public void removeItem(int position) {
        taskList.remove(position);
        notifyItemRemoved(position);
    }

    public void restoreItem(ListTask item, int position) {
        taskList.add(position, item);
        notifyItemInserted(position);
    }


    public interface ToDoTaskClickListener {
        void onClick(View view, ListTask position);
        void onChecked(ListTask position);
    }
}