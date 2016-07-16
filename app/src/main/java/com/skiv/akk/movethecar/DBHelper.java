package com.skiv.akk.movethecar;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Skiv on 07.07.2016.
 */
class DBHelper extends SQLiteOpenHelper {
    final String LOG_TAG = "myLog";


    public DBHelper(Context context) {
        // конструктор суперкласса
        super(context, "mcDB", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(LOG_TAG, "--- onCreate database ---");
        // создаем таблицу с полями
        db.execSQL("DROP TABLE IF EXISTS mc_table;");
        db.execSQL("create table mc_table ("
                + "id integer primary key autoincrement,"
                + "name text,"
                + "gn text,"
                + "phone text,"
                + "u_id integer"
                + ");");

        db.execSQL("DROP TABLE IF EXISTS mc_msg;");
        db.execSQL("create table mc_msg ("
                + "id integer primary key autoincrement,"
                + "date text,"
                + "push_id integer,"
                + "from_id integer,"
                + "title text,"
                + "msg text,"
                + "view integer"
                + ");");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // создаем таблицу с полями
        db.execSQL("DROP TABLE IF EXISTS mc_table;");
        db.execSQL("create table mc_table ("
                + "id integer primary key autoincrement,"
                + "name text,"
                + "gn text,"
                + "phone text,"
                + "u_id integer"
                + ");");

        db.execSQL("DROP TABLE IF EXISTS mc_msg;");
        db.execSQL("create table mc_msg ("
                + "id integer primary key autoincrement,"
                + "date text,"
                + "push_id integer,"
                + "from_id integer,"
                + "title text,"
                + "msg text,"
                + "view integer"
                + ");");
    }
}
