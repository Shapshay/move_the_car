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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.barryzhang.temptyview.TEmptyView;
import com.barryzhang.temptyview.TViewUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MessagesActivity extends Activity {
    DBHelper dbHelper;
    final String LOG_TAG = "myLog";
    ArrayList<Msg> arrMsg;
    BoxAdapter boxAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.messages);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            int push_id = getIntent().getExtras().getInt("push_id");
            Intent intent = new Intent(MessagesActivity.this, MsgActivity.class);
            intent.putExtra("push_id", push_id);
            startActivity(intent);
            finish();
        }

        arrMsg = new ArrayList<Msg>();

        // создаем объект для создания и управления версиями БД
        dbHelper = new DBHelper(this);
        // создаем объект для данных
        ContentValues cv = new ContentValues();
        // подключаемся к БД
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Log.d(LOG_TAG, "--- Rows in mc_msg: ---");
        // делаем запрос всех данных из таблицы mytable, получаем Cursor
        //Cursor c = db.query("mc_msg", null, null, null, null, null, null);
        String query = "SELECT * FROM mc_msg";
        Log.d(LOG_TAG, query);
        Cursor c = db.rawQuery(query, null);
        while (c.moveToNext()) {
            String date = c.getString(c.getColumnIndex("date"));
            String title = c.getString(c.getColumnIndex("title"));
            String msg = c.getString(c.getColumnIndex("msg"));
            int push_id = c.getInt(c.getColumnIndex("push_id"));
            int view = c.getInt(c.getColumnIndex("view"));
            arrMsg.add(new Msg(push_id,date,title,msg,view));
        }
        c.close();

        // создаем адаптер
        boxAdapter = new BoxAdapter(this, arrMsg);
        // настраиваем список
        ListView lvMain = (ListView) findViewById(R.id.lvMain);
        lvMain.setAdapter(boxAdapter);

        if(boxAdapter.getCount()==0){
            TViewUtil.EmptyViewBuilder emptyViewConfig = TViewUtil.EmptyViewBuilder.getInstance(getApplicationContext())
                    .setShowText(true)
                    .setEmptyText("Входящих сообщений нет")
                    .setShowButton(false)
                    .setShowIcon(true);
            TEmptyView.init(emptyViewConfig);
            TViewUtil.setEmptyView(lvMain);

            Button btnDel = (Button) findViewById(R.id.btnDel);
            btnDel.setVisibility(View.INVISIBLE);
        }
        //lvMain.setOnItemClickListener(itemClickListener);
    }


    /*BoxAdapter itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            HashMap<String, Object> itemHashMap = (HashMap <String, Object>) parent.getItemAtPosition(position);
            String titleItem = itemHashMap.get("title").toString();
            String descriptionItem = itemHashMap.get("push_id").toString();
            //int imageItem = (int)itemHashMap.get(ICON);
            Toast.makeText(getApplicationContext(),
                    "Вы выбрали " + titleItem + ". push_id: " + descriptionItem, Toast.LENGTH_SHORT).show();

        }
    };*/

    // выводим информацию о корзине
    public void showResult(View v) {
        String result = "Удалено сообщений: ";
        int clearCount = 0;
        // подключаемся к БД
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String where = "push_id = ?";
        String[] push = new String[1];
        for (Msg p : boxAdapter.getBox()) {
            if (p.box){
                push[0] = p.push_id+"";
                clearCount+= db.delete("mc_msg", where, push);
            }
           //
        }
        result += clearCount;
        Toast.makeText(this, result, Toast.LENGTH_LONG).show();
        Intent intent = new Intent(MessagesActivity.this, MessagesActivity.class);
        startActivity(intent);
        finish();
    }

    public void onMenuBtnClick(View view) {
        Intent intent = new Intent(MessagesActivity.this, MenuActivity.class);
        startActivity(intent);
    }
}
