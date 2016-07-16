package com.skiv.akk.movethecar;

/**
 * Created by Skiv on 11.07.2016.
 */
public class Msg {
    public int push_id;
    public String date;
    public String title;
    public String msg;
    public int view;
    public boolean box;

    public Msg(int push_id, String date, String title, String msg, int view) {
        this.push_id = push_id;
        this.date = date;
        this.title = title;
        this.msg = msg;
        this.view = view;
        this.box = false;
    }
}
