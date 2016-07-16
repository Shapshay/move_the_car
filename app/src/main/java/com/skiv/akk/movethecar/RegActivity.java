package com.skiv.akk.movethecar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RegActivity extends Activity {

    private ProgressDialog pDialog;

    JSONParser jsonParser = new JSONParser();
    EditText nameTxt;
    EditText phoneTxt;
    EditText gnTxt;
    DBHelper dbHelper;

    final String LOG_TAG = "myLog";
    private static String url_reg = "http://java.coap.kz/mc/reg.php";

    private static final String TAG_SUCCESS = "success";
    private static final String  TAG_MESSAGE = "message";
    private static final String  TAG_CODE = "code";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reg);

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
            Intent intent = new Intent(RegActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        else {
            Log.d(LOG_TAG, "Нет в базе");
        }

        nameTxt = (EditText) findViewById(R.id.nameTxt);
        phoneTxt = (EditText) findViewById(R.id.phoneTxt);
        gnTxt = (EditText) findViewById(R.id.gnTxt);

        nameTxt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean gainFocus) {
                //onFocus
                if (gainFocus) {
                    //set the row background to a different color
                    ((View) v).setBackgroundResource(R.color.lidhtGreen);
                }
                //onBlur
                else {
                    //set the row background white
                    ((View) v).setBackgroundResource(R.color.white);
                }
            }
        });

        phoneTxt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean gainFocus) {
                //onFocus
                if (gainFocus) {
                    //set the row background to a different color
                    ((View) v).setBackgroundResource(R.color.lidhtGreen);
                }
                //onBlur
                else {
                    //set the row background white
                    ((View) v).setBackgroundResource(R.color.white);
                }
            }
        });

        gnTxt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean gainFocus) {
                //onFocus
                if (gainFocus) {
                    //set the row background to a different color
                    ((View) v).setBackgroundResource(R.color.lidhtGreen);
                }
                //onBlur
                else {
                    //set the row background white
                    ((View) v).setBackgroundResource(R.color.white);
                }
            }
        });

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String messageExtras = getIntent().getExtras().getString("message");
            int successExtras = getIntent().getExtras().getInt("success");
            String phoneExtras = getIntent().getExtras().getString("phone");
            String nameExtras = getIntent().getExtras().getString("name");
            String gnExtras = getIntent().getExtras().getString("gn");
            if(successExtras!=1){
                phoneTxt.setText(phoneExtras);
                nameTxt.setText(nameExtras);
                gnTxt.setText(gnExtras);
                AlertDialog dialog = DialogScreen.getDialog(RegActivity.this, 3);
                dialog.setTitle(R.string.common_error_message);
                dialog.show();
            }
            else{
                Intent intent = new Intent(RegActivity.this, ActRegActivity.class);
                String codeExtras = getIntent().getExtras().getString("code");
                intent.putExtra("code", codeExtras);
                intent.putExtra("name", nameExtras);
                intent.putExtra("phone", phoneExtras);
                intent.putExtra("gn", gnExtras);
                startActivity(intent);
                finish();
            }

        }
    }


    public void onCheckReg(View view) {
        Boolean SendReg = true;
        EditText phoneEditText = (EditText) findViewById(R.id.phoneTxt);
        EditText nameEditText = (EditText) findViewById(R.id.nameTxt);
        EditText gnEditText = (EditText) findViewById(R.id.gnTxt);

        // Валидация формы
        if (phoneEditText == null || phoneEditText.length() == 0) {
            SendReg = false;
            AlertDialog dialog = DialogScreen.getDialog(RegActivity.this, 3);
            dialog.setTitle(R.string.common_error_message);
            dialog.show();
            phoneEditText.requestFocus();
        } else {
            if (nameEditText == null || nameEditText.length() == 0) {
                SendReg = false;
                AlertDialog dialog = DialogScreen.getDialog(RegActivity.this, 3);
                dialog.setTitle(R.string.common_error_message);
                dialog.show();
                nameEditText.requestFocus();
            } else {
                if (gnEditText == null || gnEditText.length() == 0) {
                    SendReg = false;
                    AlertDialog dialog = DialogScreen.getDialog(RegActivity.this, 3);
                    dialog.setTitle(R.string.common_error_message);
                    dialog.show();
                    gnEditText.requestFocus();
                }
            }
        }

        // отправка в COAP
        if (SendReg) {
            Log.d(LOG_TAG, "отправка в базу");
            new RegUser().execute(phoneEditText.getText().toString(), nameEditText.getText().toString(), gnEditText.getText().toString().toUpperCase());
        }
    }

    /**
     * Фоновый Async Task создания нового пользователя
     **/
    class RegUser extends AsyncTask<String, String, String> {

        /**
         * Перед согданием в фоновом потоке показываем прогресс диалог
         **/
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(RegActivity.this);
            pDialog.setMessage("Регистрация пользователя...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        /**
         * Создание пользователя
         **/
        protected String doInBackground(String[] args) {
            String phone = args[0];
            String name = args[1];
            String gn = args[2].toUpperCase();

            // Заполняем параметры
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("phone", phone));
            params.add(new BasicNameValuePair("name", name));
            params.add(new BasicNameValuePair("gn", gn));
            Log.d(LOG_TAG, "Start json");
            // получаем JSON объект
            JSONObject json = jsonParser.makeHttpRequest(url_reg, "POST", params);
            Log.d(LOG_TAG, "End json");
            Log.d(LOG_TAG, json.toString());

            try {
                int success = json.getInt(TAG_SUCCESS);
                Log.d(LOG_TAG, "TAG_SUCCESS = "+success);
                String message = json.getString(TAG_MESSAGE);
                Log.d(LOG_TAG, "TAG_MESSAGE = "+message);
                String code = json.getString(TAG_CODE);
                Log.d(LOG_TAG, "TAG_CODE = "+code);

                if (success == 1) {
                    Log.d(LOG_TAG, "success == 1");
                    // пользователь удачно создан
                    Intent i = new Intent(getApplicationContext(), RegActivity.class);
                    i.putExtra("phone", phone);
                    i.putExtra("name", name);
                    i.putExtra("gn", gn.toUpperCase());
                    i.putExtra("success", success);
                    i.putExtra("message", message);
                    i.putExtra("code", code);
                    startActivity(i);
                    finish();
                    // закрываем это окно
                    //finish();

                }
                else{
                    Log.d(LOG_TAG, "success == "+success);
                    // пользователь не создан
                    Intent i = new Intent(getApplicationContext(), RegActivity.class);
                    i.putExtra("phone", phone);
                    i.putExtra("name", name);
                    i.putExtra("gn", gn.toUpperCase());
                    i.putExtra("success", success);
                    i.putExtra("message", message);

                    startActivity(i);
                    finish();
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
            pDialog.dismiss();
        }

    }

    public void onMenuCutBtnClick(View view) {
        Intent intent = new Intent(RegActivity.this, MenuCutActivity.class);
        startActivity(intent);
    }
}
