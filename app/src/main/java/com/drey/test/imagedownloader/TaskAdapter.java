package com.drey.test.imagedownloader;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by drey on 01.11.2015.
 */
public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.Holder> {

    private ArrayList<Task> _data;

    public TaskAdapter (ArrayList<Task> data){
            _data = data;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.task, parent, false);
        Holder vh = new Holder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        holder.text.setText(_data.get(position).getUrl());
        holder.status.setText(_data.get(position).getStatusString());
        holder.image.setImageBitmap(_data.get(position).getImg());
    }

    @Override
    public int getItemCount() {
        return _data.size();
    }

    public static class Holder extends RecyclerView.ViewHolder {
        public TextView text;
        public TextView status;
        public ImageView image;

        public Holder(View v) {
            super(v);
            text = (TextView) v.findViewById(R.id.text);
            status = (TextView) v.findViewById(R.id.status);
            image = (ImageView) v.findViewById(R.id.image);
        }
    }

    public void addTask(Task t){
        _data.add(t);
    }
}
