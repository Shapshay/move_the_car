package com.skiv.akk.movethecar;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ActRegActivity extends AppCompatActivity {

    private ProgressDialog pDialog;
    final String LOG_TAG = "myLog";

    public static String url_act_reg = "http://java.coap.kz/mc/act_reg.php";
    DBHelper dbHelper;
    TextView reg_result;
    TextView serv_text;

    private static final String TAG_SUCCESS = "success";
    private static final String  TAG_MESSAGE = "message";
    private static final String  TAG_CODE = "code";
    private static final String  TAG_U_ID = "u_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_reg);
        reg_result = (TextView) findViewById(R.id.reg_result);
        serv_text = (TextView) findViewById(R.id.serv_text);

        // создаем объект для создания и управления версиями БД
        dbHelper = new DBHelper(this);
        // создаем объект для данных
        ContentValues cv = new ContentValues();
        // подключаемся к БД
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Log.d(LOG_TAG, "--- Rows in mytable: ---");
        // делаем запрос всех данных из таблицы mytable, получаем Cursor
        Cursor c = db.query("mc_table", null, null, null, null, null, null);
        if (c.moveToFirst()) {
            Log.d(LOG_TAG, "Есть в базе");
            Intent intent = new Intent(ActRegActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        else {
            Log.d(LOG_TAG, "Нет в базе");
        }

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String messageExtras = getIntent().getExtras().getString("code");
            serv_text.setText(messageExtras);
        }
        Log.d(LOG_TAG, "GetSMSList == 1");
        try {
            GetSMSList();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.d(LOG_TAG, "GetSMSList == 2");
        String code1 = reg_result.getText().toString();
        String code2 = serv_text.getText().toString();
        code1 = code1.replace("Code: ", "");
        Log.d(LOG_TAG, "code1 == "+code1);
        Log.d(LOG_TAG, "code2 == "+code2);
        if(code1.compareTo(code2)==0){
            new ActRegUser().execute();

        }
        else{
            Intent intent = new Intent(ActRegActivity.this, RegActivity.class);
            intent.putExtra("phone", "");
            intent.putExtra("name", "");
            intent.putExtra("gn", "");
            intent.putExtra("success", "0");
            intent.putExtra("message", R.string.err_act_code);
            startActivity(intent);
            finish();
        }
    }

    public  void GetSMSList() throws InterruptedException {

        pDialog = new ProgressDialog(ActRegActivity.this);
        pDialog.setMessage("Активация приложения...");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(true);
        pDialog.show();

        TextView mText = (TextView) findViewById(R.id.reg_result);
        Boolean SMSfind = (Boolean) false;
        String tmpSmsBody;
        Thread.sleep(5000);
        Uri uriSms;
        Context context;
        Cursor cur;


        uriSms = Uri.parse("content://sms/inbox");
        context = this;
        cur = context.getContentResolver().query(uriSms, null, null, null, null);
            startManagingCursor(cur);
        do {
            if (cur.moveToFirst()) { // must check the result to prevent exception
                do {
                    String msgData = "";
                    for(int idx=0;idx<cur.getColumnCount();idx++)
                    {
                        String id_sms = "";
                        //Log.d(LOG_TAG, "cur.getColumnName(idx) == "+cur.getColumnName(idx));
                        if("_id".equals(cur.getColumnName(idx))) {
                            id_sms = cur.getString(idx);
                            Log.d(LOG_TAG, "id_sms == "+id_sms);
                        }
                        if("body".equals(cur.getColumnName(idx))) {
                            //msgData += " " + cur.getColumnName(idx) + ":" + cur.getString(idx);
                            tmpSmsBody =  cur.getString(idx);
                            Log.d(LOG_TAG, "tmpSmsBody == "+tmpSmsBody);
                            if(tmpSmsBody.contains("Code: ")) {
                                mText.append(tmpSmsBody);
                                SMSfind = true;
                                context.getContentResolver().delete(Uri.parse("content://sms/" + id_sms), null, null); //id - идентификатор сообщения
                                break;
                            }
                        }
                    }
                    // use msgData
                    //Log.d(LOG_TAG, "msgData == "+msgData);
                } while (cur.moveToNext());
            }
        }while (!SMSfind);

        /**
         * После оконачния скрываем прогресс диалог
         **/
        pDialog.dismiss();
    }

    class ActRegUser extends AsyncTask<String, String, String> {

        /**
         * Перед согданием в фоновом потоке показываем прогресс диалог
         **/
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(ActRegActivity.this);
            pDialog.setMessage("Активация приложения...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        /**
         * Создание пользователя
         **/
        protected String doInBackground(String[] args) {
            String phoneExtras = getIntent().getExtras().getString("phone");
            String nameExtras = getIntent().getExtras().getString("name");
            String gnExtras = getIntent().getExtras().getString("gn");

            // Заполняем параметры
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("phone", phoneExtras));
            params.add(new BasicNameValuePair("gn", gnExtras));
            Log.d(LOG_TAG, "Start json");
            // получаем JSON объект

            JSONParser jsonParser_act = new JSONParser();
            String url_act_reg = "http://java.coap.kz/mc/act_reg.php";
            JSONObject json_act = null;
            json_act = jsonParser_act.makeHttpRequest(url_act_reg, "POST", params);
            Log.d(LOG_TAG, "url_act_reg="+url_act_reg);
            Log.d(LOG_TAG, "End json");
            Log.d(LOG_TAG, json_act.toString());

            try {
                int success = json_act.getInt(TAG_SUCCESS);
                Log.d(LOG_TAG, "TAG_SUCCESS_ACT = "+success);
                String message = json_act.getString(TAG_MESSAGE);
                Log.d(LOG_TAG, "TAG_MESSAGE_ACT = "+message);
                String u_id = json_act.getString(TAG_U_ID);
                Log.d(LOG_TAG, "TAG_U_ID_ACT = "+u_id);

                Log.d(LOG_TAG, "Create DB");
                // создаем объект для создания и управления версиями БД
                Log.d(LOG_TAG, "Conect DB");
                // подключаемся к БД
                SQLiteDatabase db = dbHelper.getWritableDatabase();

                Log.d(LOG_TAG, "Data DB");
                // создаем объект для данных
                ContentValues cv = new ContentValues();

                // подготовим данные для вставки в виде пар: наименование столбца - значение
                cv.put("phone", phoneExtras);
                cv.put("name", nameExtras);
                cv.put("gn", gnExtras);
                cv.put("u_id", u_id);
                //очищаем базу
                int clearCount = db.delete("mc_table", null, null);
                Log.d(LOG_TAG, "deleted rows count = " + clearCount);
                // вставляем запись и получаем ее ID
                long rowID = db.insert("mc_table", null, cv);
                Log.d(LOG_TAG, "row inserted, ID = " + rowID);





           } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        /**
         * После оконачния скрываем прогресс диалог
         **/
        protected void onPostExecute(String file_url) {
            //pDialog.dismiss();
            Intent intent = new Intent(ActRegActivity.this, MainActivity.class);
            intent.putExtra("activ", 1);
            startActivity(intent);
            finish();
        }

    }
}
