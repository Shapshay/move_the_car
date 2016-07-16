package com.skiv.akk.movethecar;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class MsgActivity extends Activity {
    DBHelper dbHelper;
    final String LOG_TAG = "myLog";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.msg);
        int push_id = getIntent().getExtras().getInt("push_id");
        TextView textDate = (TextView) findViewById(R.id.textDate);
        TextView textTitle = (TextView) findViewById(R.id.textTitle);
        TextView textMsg = (TextView) findViewById(R.id.textMsg);

        // создаем объект для создания и управления версиями БД
        dbHelper = new DBHelper(this);
        // создаем объект для данных
        ContentValues cv = new ContentValues();
        // подключаемся к БД
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Log.d(LOG_TAG, "--- Rows in mc_msg: ---");
        // делаем запрос всех данных из таблицы mytable, получаем Cursor
        //Cursor c = db.query("mc_msg", null, null, null, null, null, null);
        String query = "SELECT * FROM mc_msg WHERE push_id = "+push_id+" LIMIT 1";
        Cursor c = db.rawQuery(query, null);
        if (c.moveToFirst()) {
            String date = c.getString(c.getColumnIndex("date"));
            String title = c.getString(c.getColumnIndex("title"));
            String msg = c.getString(c.getColumnIndex("msg"));
            textDate.setText(date);
            textTitle.setText(title);
            textMsg.setText(msg);
        }
        c.close();

        ContentValues values = new ContentValues();
        values.put("view", 1);
        db.update("mc_msg",
                values,
                "push_id = ?", new String[]{Integer.toString(push_id)});
    }

    public void onCheckMsg(View view) {
        Intent intent = new Intent(MsgActivity.this, MessagesActivity.class);
        startActivity(intent);
        finish();
    }

    public void onMenuBtnClick(View view) {
        Intent intent = new Intent(MsgActivity.this, MenuActivity.class);
        startActivity(intent);
    }
}
