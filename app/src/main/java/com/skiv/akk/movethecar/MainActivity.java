package com.skiv.akk.movethecar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.github.javiersantos.bottomdialogs.BottomDialog;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private ProgressDialog pDialog;

    JSONParser jsonParser = new JSONParser();
    EditText gnTxt;
    DBHelper dbHelper;

    final String LOG_TAG = "myLog";
    private static String url_send = "http://java.coap.kz/mc/send.php";

    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        getApplicationContext().startService(new Intent(getApplicationContext(), McPushService.class));

        gnTxt = (EditText) findViewById(R.id.gnTxt);

        dbHelper = new DBHelper(this);

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

        // reklama
        BottomDialog bottomDialog = new BottomDialog.Builder(this)
                .setTitle("Рекламный заголовок!")
                .setContent("Текст рекламного объявления.")
                .setIcon(R.drawable.ic_launcher)
                .setCancelable(true)
                .setNegativeText("Отмена")
                /*.onNegative(new BottomDialog.ButtonCallback() {
                    @Override
                    public void onClick(BottomDialog dialog) {
                        Log.d("BottomDialogs", "Do something!");
                    })*/
                .setPositiveText("Перейти")
                .onPositive(new BottomDialog.ButtonCallback() {
                    @Override
                    public void onClick(BottomDialog dialog) {
                        Log.d("BottomDialogs", "Do something!");
                    }
                }).show();





        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            int successExtras = getIntent().getExtras().getInt("success");
            String messageExtras = getIntent().getExtras().getString("message");
            String gnExtras = getIntent().getExtras().getString("gn");
            if(gnExtras!=""&&gnExtras!=null) {
                switch (successExtras) {
                    case 1: {
                        AlertDialog dialog = DialogScreen.getDialog(MainActivity.this, 2);
                        dialog.setMessage(messageExtras);
                        dialog.show();
                        break;
                    }
                    case 0: {
                        AlertDialog dialog = DialogScreen.getDialog(MainActivity.this, 3);
                        dialog.setMessage(messageExtras);
                        dialog.show();
                        gnTxt.setText(gnExtras);
                        break;
                    }
                    case 2: {
                        AlertDialog dialog = DialogScreen.getDialog(MainActivity.this, 3);
                        dialog.setMessage(messageExtras);
                        dialog.show();
                        gnTxt.setText(gnExtras);
                        break;
                    }
                }
            }
        }



    }

    public void onSendGn(View view) {
        Boolean SendReg = true;
        EditText gnEditText = (EditText) findViewById(R.id.gnTxt);

        if (gnEditText == null || gnEditText.length() == 0) {
            SendReg = false;
            AlertDialog dialog = DialogScreen.getDialog(MainActivity.this, 3);
            dialog.setTitle(R.string.common_error_message);
            dialog.show();
            gnEditText.requestFocus();
        }

        // отправка в COAP
        if (SendReg) {
            Log.d(LOG_TAG, "отправка в базу");
            // подключаемся к БД
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            Log.d(LOG_TAG, "--- Rows in mytable: ---");
            // делаем запрос всех данных из таблицы mytable, получаем Cursor
            Cursor c = db.query("mc_table", null, null, null, null, null, null);

            // ставим позицию курсора на первую строку выборки
            // если в выборке нет строк, вернется false
            String u_id = "0";
            if (c.moveToFirst()) {
                int u_idColIndex = c.getColumnIndex("u_id");
                u_id = c.getString(u_idColIndex);
            }
            new sendGn().execute(gnEditText.getText().toString().toUpperCase(), u_id);
        }
    }



    /**
     * Фоновый Async Task создания нового пользователя
     **/
    class sendGn extends AsyncTask<String, String, String> {

        /**
         * Перед отправкой в фоновом потоке показываем прогресс диалог
         **/
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Отправка...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        /**
         * Отправка сообщения
         **/
        protected String doInBackground(String[] args) {
            String gn = args[0].toUpperCase();
            String u_id = args[1];

            // Заполняем параметры
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("gn", gn));
            params.add(new BasicNameValuePair("u_id", u_id));
            Log.d(LOG_TAG, "Start json");
            // получаем JSON объект
            JSONObject send_json = jsonParser.makeHttpRequest(url_send, "POST", params);
            Log.d(LOG_TAG, "End json");
            Log.d(LOG_TAG, send_json.toString());

            try {
                int success = send_json.getInt(TAG_SUCCESS);
                Log.d(LOG_TAG, "TAG_SUCCESS = " + success);
                String message = send_json.getString(TAG_MESSAGE);
                Log.d(LOG_TAG, "TAG_MESSAGE = " + message);
                if (success == 1) {
                    Log.d(LOG_TAG, "success == 1");
                    // пользователь удачно создан
                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
                    i.putExtra("gn", gn.toUpperCase());
                    i.putExtra("success", success);
                    i.putExtra("message", message);
                    startActivity(i);
                    finish();
                    // закрываем это окно
                    finish();

                } else {
                    Log.d(LOG_TAG, "success == " + success);
                    // пользователь не создан
                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
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
    }

    public void onMenuBtnClick(View view) {
        Intent intent = new Intent(MainActivity.this, MenuActivity.class);
        startActivity(intent);
    }
}
