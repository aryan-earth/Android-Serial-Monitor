package com.qass.serialmonitor;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import java.util.List;

public class BtAdapter extends RecyclerView.Adapter<BtAdapter.BtCommandsViewHolder>{

    //this context will be used to inflate the layout
    private Context mCtx;

    //we are storing all the history in a list
    private List<BtCommands> commandsList;
    public static Boolean flag = false;

    //getting the context and history list with constructor
    public BtAdapter(Context mCtx, List<BtCommands> commandsList) {
        this.mCtx = mCtx;
        this.commandsList = commandsList;
    }


    @Override
    public BtCommandsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //inflating and returning our view holder
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.layout_bt_item, null);
        return new BtCommandsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(BtCommandsViewHolder holder, int position) {
        //getting the product of the specified position
        BtCommands commands = commandsList.get(position);
        //binding the data with the viewholder views
        holder.textViewCommand.setText(commands.getCommand());
        if(flag) {
            holder.textViewCommand.setTextColor(Color.parseColor("#F4D03F"));
            flag = false;
        }
        holder.dt.setText(commands.getTime());
        holder.dt.setText(commands.getTime());
    }


    @Override
    public int getItemCount() {
        return commandsList.size();
    }

    class BtCommandsViewHolder extends RecyclerView.ViewHolder {

        TextView textViewCommand;
        TextView dt;


        public BtCommandsViewHolder(View itemView) {
            super(itemView);
            textViewCommand = itemView.findViewById(R.id.bt_command_textview);
            dt  = itemView.findViewById(R.id.date_time_textview);
        }
    }
}
