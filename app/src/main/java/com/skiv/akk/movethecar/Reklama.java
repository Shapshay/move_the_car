package com.skiv.akk.movethecar;

/**
 * Created by Skiv on 18.07.2016.
 */
public class Reklama {
    public int id;
    public String date;
    public String title;
    public String msg;
    public String icon;
    public String url;
    public int view;

    public Reklama(int id, String date, String title, String msg, String icon, String url, int view) {
        this.id = id;
        this.date = date;
        this.title = title;
        this.msg = msg;
        this.icon = icon;
        this.url = url;
        this.view = view;
    }

    public Reklama() {
        this.id = 0;
        this.date = null;
        this.title = null;
        this.msg = null;
        this.icon = null;
        this.url = null;
        this.view = 0;
    }
}
