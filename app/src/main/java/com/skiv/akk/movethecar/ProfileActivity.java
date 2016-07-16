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
import android.widget.ListView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import android.view.ViewGroup.LayoutParams;

public class ProfileActivity extends Activity {

    DBHelper dbHelper;

    EditText nameTxt;
    EditText gnTxt;

    public static ArrayList<GosNomer> gns;

    public static GnAdapter gnAdapter;

    private ProgressDialog pDialog;

    JSONParser jsonParser = new JSONParser();

    final String LOG_TAG = "myLog";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);

        dbHelper = new DBHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor c = db.query("mc_table", null, null, null, null, null, null);
        int u_id = 0;
        String name = "";
        gns = new ArrayList<GosNomer>();

        while (c.moveToNext()) {
            int u_idColIndex = c.getColumnIndex("u_id");
            u_id = Integer.parseInt(c.getString(u_idColIndex));
            int nameColIndex = c.getColumnIndex("name");
            name = c.getString(nameColIndex);
            int gnColIndex = c.getColumnIndex("gn");
            Log.d(LOG_TAG, "BD gn = "+c.getString(gnColIndex));

            gns.add(new GosNomer(c.getString(gnColIndex)));
        }
        c.close();

        /*for(GosNomer s:gns){
            Log.d(LOG_TAG, "Arr gn = "+s.nomer);
        }*/

        gnTxt = (EditText) findViewById(R.id.gnTxt);
        nameTxt = (EditText) findViewById(R.id.nameTxt);

        nameTxt.setText(name);

        // создаем адаптер
        gnAdapter = new GnAdapter(this, gns);
        // настраиваем список
        ListView lvMain = (ListView) findViewById(R.id.lvMain);
        lvMain.setAdapter(gnAdapter);

        LayoutParams listLP = (LayoutParams) lvMain.getLayoutParams();
        listLP.height = gnAdapter.getCount()*100;//like int  200
        lvMain.setLayoutParams(listLP);
    }



    // изменяем имя пользователя
    public void onSendName(View view) {
        Boolean SendReg = true;
        EditText nameEditText = (EditText) findViewById(R.id.nameTxt);

        // Валидация формы
        if (nameEditText == null || nameEditText.length() == 0) {
                SendReg = false;
                AlertDialog dialog = DialogScreen.getDialog(ProfileActivity.this, 3);
                dialog.setTitle(R.string.common_error_message);
                dialog.setMessage("Заполните имя!");
                dialog.show();
                nameEditText.requestFocus();
        }

        // отправка в COAP
        if (SendReg) {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            Cursor c = db.query("mc_table", null, null, null, null, null, null);
            String u_id = "0";
            if (c.moveToFirst()) {
                int u_idColIndex = c.getColumnIndex("u_id");
                u_id = c.getString(u_idColIndex);
            }
            new ChangeUser().execute(nameEditText.getText().toString(), u_id);
        }
    }

    /**
     * Фоновый Async Task изменения пользователя
     **/
    class ChangeUser extends AsyncTask<String, String, String> {

        /**
         * Перед отправкой в фоновом потоке показываем прогресс диалог
         **/
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(ProfileActivity.this);
            pDialog.setMessage("Отправка...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        /**
         * Отправка сообщения
         **/
        protected String doInBackground(String[] args) {
            String name = args[0];
            String u_id = args[1];
            String url_change = "http://java.coap.kz/mc/change.php";

            // Заполняем параметры
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("name", name));
            params.add(new BasicNameValuePair("u_id", u_id));
            Log.d(LOG_TAG, "NAME = "+name);
            // получаем JSON объект
            JSONObject send_json = jsonParser.makeHttpRequest(url_change, "POST", params);
            Log.d(LOG_TAG, send_json.toString());

            try {
                int success = send_json.getInt("success");
                String message = send_json.getString("message");
                if (success == 1) {
                    Log.d(LOG_TAG, "success == 1");
                    // пользователь удачно изменен
                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    ContentValues values = new ContentValues();
                    values.put("name", name);
                    db.update("mc_table", values, null, null);
                    /*Toast.makeText(getApplicationContext(),
                            message, Toast.LENGTH_SHORT).show();*/

                } else {
                    Log.d(LOG_TAG, "success == " + success);
                    // пользователь не изменен
                    Log.d(LOG_TAG, "message == " + message);
                    /*Toast.makeText(getApplicationContext(),
                            "Ошибка изменения:\n" + message, Toast.LENGTH_SHORT).show();*/

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

    // добавляем гос.номер
    public void onAddGn(View view) {
        Boolean SendGn = true;
        EditText gnEditText = (EditText) findViewById(R.id.gnTxt);

        // Валидация формы
        if (gnEditText == null || gnEditText.length() == 0) {
            SendGn = false;
            AlertDialog dialog = DialogScreen.getDialog(ProfileActivity.this, 3);
            dialog.setTitle(R.string.common_error_message);
            dialog.setMessage("Заполните гос.номер!");
            dialog.show();
            gnEditText.requestFocus();
        }

        // отправка в COAP
        if (SendGn) {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            Cursor c = db.query("mc_table", null, null, null, null, null, null);
            String u_id = "0";
            String name = "";
            String phone = "";
            if (c.moveToFirst()) {
                int u_idColIndex = c.getColumnIndex("u_id");
                u_id = c.getString(u_idColIndex);
                int nameColIndex = c.getColumnIndex("name");
                name = c.getString(nameColIndex);
                int phoneColIndex = c.getColumnIndex("phone");
                phone = c.getString(phoneColIndex);
            }
            new AddGn().execute(gnEditText.getText().toString().toUpperCase(), u_id, name, phone);
        }
    }

    /**
     * Фоновый Async Task добавления гос.номера
     **/
    class AddGn extends AsyncTask<String, String, String> {

        /**
         * Перед отправкой в фоновом потоке показываем прогресс диалог
         **/
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(ProfileActivity.this);
            pDialog.setMessage("Отправка...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        /**
         * Отправка сообщения
         **/
        protected String doInBackground(String[] args) {
            String gn = args[0];
            String u_id = args[1];
            String name = args[2];
            String phone = args[3];
            String url_change = "http://java.coap.kz/mc/add_gn.php";

            // Заполняем параметры
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("gn", gn));
            params.add(new BasicNameValuePair("u_id", u_id));
            // получаем JSON объект
            JSONObject send_json = jsonParser.makeHttpRequest(url_change, "POST", params);
            Log.d(LOG_TAG, send_json.toString());

            try {
                int success = send_json.getInt("success");
                String message = send_json.getString("message");
                if (success == 1) {
                    Log.d(LOG_TAG, "success == 1");
                    // гос.номер добавлен
                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    ContentValues values = new ContentValues();
                    values.put("name", name);
                    values.put("gn", gn);
                    values.put("phone", phone);
                    values.put("u_id", u_id);
                    db.insert("mc_table", null, values);


                    /*Toast.makeText(getApplicationContext(),
                            message, Toast.LENGTH_SHORT).show();*/
                    ProfileActivity.gns.add(new GosNomer(gn));

                    //boxAdapter.notifyDataSetChanged();

                } else {
                    Log.d(LOG_TAG, "success == " + success);
                    // гос.номер не добавлен
                    Log.d(LOG_TAG, "message == " + message);
                    /*Toast.makeText(getApplicationContext(),
                            "Ошибка добавления:\n" + message, Toast.LENGTH_SHORT).show();*/

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
            Intent intent = new Intent(ProfileActivity.this, ProfileActivity.class);
            startActivity(intent);
            finish();
        }
    }


    // удаляем гос.номер
    public void onDelGn(View v) {
        String result = "Удалено автомобилей: ";
        int clearCount = 0;
        int selectCount = 0;
        int allCount = gnAdapter.getCount();

        for (GosNomer p : gnAdapter.getBox()) {
            if (p.box)
                selectCount++;
        }
        Log.d(LOG_TAG, "allCount == " + allCount);
        Log.d(LOG_TAG, "selectCount == " + selectCount);
        if(allCount>1&&selectCount<allCount) {
            // подключаемся к БД
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            Cursor c = db.query("mc_table", null, null, null, null, null, null);
            String u_id = "0";
            if (c.moveToFirst()) {
                int u_idColIndex = c.getColumnIndex("u_id");
                u_id = c.getString(u_idColIndex);
            }
            String where = "gn = ?";
            String[] del_gn = new String[1];
            for (GosNomer p : gnAdapter.getBox()) {
                if (p.box) {
                    del_gn[0] = p.nomer;
                    new DelGn().execute(p.nomer, u_id);
                    clearCount += db.delete("mc_table", where, del_gn);
                    gns.remove(p);
                }
            }
            result += clearCount;
            Toast.makeText(this, result, Toast.LENGTH_LONG).show();
            //boxAdapter.notifyDataSetChanged();
        }
        else{
            Toast.makeText(this, "Нельзя удалить все автомобили !", Toast.LENGTH_LONG).show();
        }
    }


    /**
     * Фоновый Async Task добавления гос.номера
     **/
    class DelGn extends AsyncTask<String, String, String> {

        /**
         * Перед отправкой в фоновом потоке показываем прогресс диалог
         **/
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(ProfileActivity.this);
            pDialog.setMessage("Отправка...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        /**
         * Отправка сообщения
         **/
        protected String doInBackground(String[] args) {
            String gn = args[0];
            String u_id = args[1];
            String url_del = "http://java.coap.kz/mc/del_gn.php";

            // Заполняем параметры
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("gn", gn));
            params.add(new BasicNameValuePair("u_id", u_id));
            // получаем JSON объект
            JSONObject send_json = jsonParser.makeHttpRequest(url_del, "POST", params);
            Log.d(LOG_TAG, send_json.toString());

            try {
                int success = send_json.getInt("success");
                String message = send_json.getString("message");
                if (success == 1) {
                    Log.d(LOG_TAG, "success == 1");
                    // гос.номер удален
                } else {
                    Log.d(LOG_TAG, "success == " + success);
                    Log.d(LOG_TAG, "message == " + message);
                    // гос.номер не удален
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
            Intent intent = new Intent(ProfileActivity.this, ProfileActivity.class);
            startActivity(intent);
            finish();
        }
    }

    public void onMenuBtnClick(View view) {
        Intent intent = new Intent(ProfileActivity.this, MenuActivity.class);
        startActivity(intent);
    }
}
