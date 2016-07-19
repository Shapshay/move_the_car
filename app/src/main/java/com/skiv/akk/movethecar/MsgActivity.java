package com.skiv.akk.movethecar;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MsgActivity extends Activity {
    DBHelper dbHelper;
    final String LOG_TAG = "myLog";
    private ProgressDialog pDialog;

    JSONParser jsonParser = new JSONParser();
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

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
        new sendAnswer().execute(Integer.toString(push_id));
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


    /**
     * Фоновый Async Task отправки подтверждения прочтения
     **/
    class sendAnswer extends AsyncTask<String, String, String> {

        /**
         * Перед отправкой в фоновом потоке показываем прогресс диалог
         **/
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try {
                pDialog = new ProgressDialog(MsgActivity.this);
                pDialog.setMessage("Отправка...");
                pDialog.setIndeterminate(false);
                pDialog.setCancelable(true);
                pDialog.show();
            }catch (Exception e){

            }
        }

        /**
         * Отправка сообщения
         **/
        protected String doInBackground(String[] args) {
            String push_id = args[0];
            String url_answer = "http://java.coap.kz/mc/answer_send.php";

            // Заполняем параметры
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("push_id", push_id));
            Log.d(LOG_TAG, "Start json");
            // получаем JSON объект
            JSONObject send_json = jsonParser.makeHttpRequest(url_answer, "POST", params);
            Log.d(LOG_TAG, "End json");
            Log.d(LOG_TAG, send_json.toString());

            try {
                int success = send_json.getInt(TAG_SUCCESS);
                Log.d(LOG_TAG, "TAG_SUCCESS = " + success);
                String message = send_json.getString(TAG_MESSAGE);
                Log.d(LOG_TAG, "TAG_MESSAGE = " + message);
                if (success == 1) {
                    Log.d(LOG_TAG, "success == 1");
                } else {
                    Log.d(LOG_TAG, "success == " + success);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        /**
         * После оконачния скрываем прогресс диалог
         **/
        protected void onPostExecute(String file_url) {
            try {
                pDialog.dismiss();
            }catch (Exception e){

            }
        }
    }
}
