package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AlarmAdapter
        extends RecyclerView.Adapter<
        AlarmAdapter.ViewHolder> {

    private List<Alarm> alarms;

    public AlarmAdapter(List<Alarm> alarms) {
        this.alarms = alarms;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view =
                LayoutInflater.from(
                                parent.getContext())
                        .inflate(
                                R.layout.item_alarm,
                                parent,
                                false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position) {

        Alarm alarm = alarms.get(position);

        holder.time.setText(
                String.format(
                        "%02d:%02d",
                        alarm.getHour(),
                        alarm.getMinute()));

        holder.label.setText(
                alarm.getLabel());
    }

    @Override
    public int getItemCount() {
        return alarms.size();
    }

    static class ViewHolder
            extends RecyclerView.ViewHolder {

        TextView time;
        TextView label;

        public ViewHolder(View itemView) {

            super(itemView);

            time =
                    itemView.findViewById(
                            R.id.txtTime);

            label =
                    itemView.findViewById(
                            R.id.txtLabel);
        }
    }
}