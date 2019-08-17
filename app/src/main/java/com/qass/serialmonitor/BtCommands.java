package com.qass.serialmonitor;

import java.text.SimpleDateFormat;

public class BtCommands {
    private int id;
    private String time;
    private String command;

    public BtCommands(int id, String c) {
        this.id = id;
        this.command = c;
    }

    public int getId() {
        return id;
    }

    public String getCommand() {
        return command;
    }

    public String getTime() {
        DateAndTime();
        return time;
    }

    public void DateAndTime() {
        //Get current date and time
        long date = System.currentTimeMillis();

        SimpleDateFormat sdf = new SimpleDateFormat("MMM MM dd, yyyy h:mm a");
        time  = sdf.format(date);
    }

}
