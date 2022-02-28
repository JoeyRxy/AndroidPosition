package com.sjtu.demoapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sjtu.demoapp.database.InfoDatabase;

import java.util.List;
import java.util.Map;
import java.util.zip.Inflater;

public class ShowInfoActivity extends AppCompatActivity {
    int related;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_info_activity);
        RecyclerView rv = findViewById(R.id.rv);
        rv.setLayoutManager(new LinearLayoutManager(this));
        Intent intent = getIntent();
        related = intent.getIntExtra("related", Integer.MIN_VALUE);
//        rv.setAdapter(new InfoAdapter(MessageHolder.getINSTANCE().Infos, this));
    }

    @Override
    protected void onResume() {
        super.onResume();
        RecyclerView rv = findViewById(R.id.rv);
        InfoAdapter adapter;
        if(related != Integer.MIN_VALUE) {
            adapter = new InfoAdapter(InfoDatabase.getInstance().infoDao().loadData(related), this);
        } else {
            adapter = new InfoAdapter(InfoDatabase.getInstance().infoDao().loadAll(), this);
        }
        rv.setAdapter(adapter);
    }
}

class InfoAdapter extends RecyclerView.Adapter<InfoAdapter.InfoViewHolder> {
    List<InfoStruct> data;
    Context mContext;
    public void setData(List<InfoStruct> data) {
        this.data = data;
    }

    public InfoAdapter(List<InfoStruct> data, Context mContext) {
        this.data = data;
        this.mContext = mContext;
    }

    static class InfoViewHolder extends RecyclerView.ViewHolder {
        public TextView tv;
        public InfoViewHolder(@NonNull View itemView) {
            super(itemView);
            tv = itemView.findViewById(R.id.tv);
        }
    }
    @NonNull
    @Override
    public InfoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext).inflate(R.layout.info_view_holder, parent, false);
        return new InfoViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull InfoViewHolder holder, int position) {
        StringBuilder sb = new StringBuilder();
        InfoStruct d = data.get(position);
        sb.append("x : ").append(d.x).append(", y : ").append(d.y).append('\n').append("F : ").append(d.floor).append('\n');
        for (Map.Entry<Integer, Integer> entry : d.cellSignalStrengthMap.entrySet()) {
            sb.append(entry.getKey()).append(" : ").append(entry.getValue()).append('\n');
        }
        holder.tv.setText(sb.toString());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}