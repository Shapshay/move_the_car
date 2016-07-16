package com.skiv.akk.movethecar;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ContactsActivity extends Activity {

    private ListView List_For_Store_Contact_Info;
    ArrayList<String> Contact_Info = new ArrayList<String>();
    private ArrayList<HashMap<String, Object>> mCatList;
    private static final String TITLE = "name"; // Верхний текст
    private static final String DESCRIPTION = "phone"; // ниже главного
    private static final String ICON = "icon";  // будущая картинка

    DBHelper dbHelper;
    private static String u_id;
    private static String u_phone;
    JSONParser jsonParser = new JSONParser();
    private static String url_invite = "http://java.coap.kz/mc/sms_invite.php";
    private ProgressDialog pDialog;
    final String LOG_TAG2 = "myInviter";
    private static final String TAG_SUCCESS = "success";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contacts);

        List_For_Store_Contact_Info=(ListView)findViewById(R.id.list);
        Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[] {
                        ContactsContract.CommonDataKinds.Phone._ID,
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.NUMBER,
                        ContactsContract.CommonDataKinds.Phone.PHOTO_URI
                }, null, null, null);
        startManagingCursor(cursor);

        // создаем массив списков
        mCatList = new ArrayList<HashMap<String, Object>>();
        HashMap<String, Object> hm;

        if (cursor.getCount() > 0)
        {
            /*while (cursor.moveToNext())
            {
                //Contact_Info.add(" ID "+cursor.getString(0)+" NAME"+cursor.getString(1)+" PHONE "+cursor.getString(2));
                Contact_Info.add("NAME = "+cursor.getString(1)+" PHONE = "+cursor.getString(2));
            }

            List_For_Store_Contact_Info.setAdapter(new ArrayAdapter<String>(this,R.layout.contacts_list_item , Contact_Info));*/
            while (cursor.moveToNext()) {
                hm = new HashMap<>();
                hm.put(TITLE, cursor.getString(1)); // Название
                hm.put(DESCRIPTION, cursor.getString(2)); // Описание
                hm.put(ICON, cursor.getString(3)); // Картинка
                mCatList.add(hm);
            }
            /*SimpleAdapter adapter = new SimpleAdapter(this, mCatList,
                    R.layout.contacts_list_item, new String[]{TITLE, DESCRIPTION, ICON},
                    new int[]{R.id.text1, R.id.text2, R.id.img});*/
            SimpleAdapter adapter = new SimpleAdapter(this, mCatList,
                    R.layout.contacts_list_item, new String[]{TITLE, DESCRIPTION, ICON},
                    new int[]{R.id.text1, R.id.text2});

            List_For_Store_Contact_Info.setAdapter(adapter);

            List_For_Store_Contact_Info.setOnItemClickListener(itemClickListener);
        }
    }

    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            HashMap<String, Object> itemHashMap = (HashMap <String, Object>) parent.getItemAtPosition(position);
            String titleItem = itemHashMap.get(TITLE).toString();
            String descriptionItem = itemHashMap.get(DESCRIPTION).toString();
            //int imageItem = (int)itemHashMap.get(ICON);
            Toast.makeText(getApplicationContext(),
                    "Вы выбрали " + titleItem + ". Приглашение будет отправлено на номер: " + descriptionItem, Toast.LENGTH_SHORT).show();
            Log.v(LOG_TAG2, "отправка в COAP");
            // создаем объект для создания и управления версиями БД
            dbHelper = new DBHelper(ContactsActivity.this);
            // создаем объект для данных
            ContentValues cv = new ContentValues();

            // подключаемся к БД
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            String LOG_TAG = "log_sms";
            Log.d(LOG_TAG, "--- Rows in mytable: ---");
            // делаем запрос всех данных из таблицы mytable, получаем Cursor
            Cursor c = db.query("mc_table", null, null, null, null, null, null);

            // ставим позицию курсора на первую строку выборки
            // если в выборке нет строк, вернется false
            if (c.moveToFirst()) {
                int u_idColIndex = c.getColumnIndex("u_id");
                u_id = c.getString(u_idColIndex);
                int u_phoneColIndex = c.getColumnIndex("phone");
                u_phone = c.getString(u_phoneColIndex);
            } else {
                u_id = "0";
                u_phone = "12345";
            }
            Log.d(LOG_TAG, "u_phone="+u_phone);
            new InviteSMS().execute(u_id,titleItem,descriptionItem,u_phone);
        }
    };

    /**
     * Фоновый Async Task отправки Invite SMS
     **/
    class InviteSMS extends AsyncTask<String, String, String> {

        /**
         * Перед созданием в фоновом потоке показываем прогресс диалог
         **/
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(ContactsActivity.this);
            pDialog.setMessage("Подключение к серверу...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        /**
         * Проверка пользователя
         **/
        protected String doInBackground(String[] args) {
            String u_id = args[0];
            String name = args[1];
            String phone = args[2];
            String u_phone = args[3];

            // Заполняем параметры
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("u_id", u_id));
            params.add(new BasicNameValuePair("name", name));
            params.add(new BasicNameValuePair("phone", phone));
            params.add(new BasicNameValuePair("u_phone", u_phone));
            Log.v(LOG_TAG2, "Start json SMS");
            // получаем JSON объект
            JSONObject json = jsonParser.makeHttpRequest(url_invite, "POST", params);
            Log.v(LOG_TAG2, "End json SMS");
            //Log.d(LOG_TAG2, json.toString());

            try {
                int success = json.getInt(TAG_SUCCESS);
                Log.v(LOG_TAG2, "TAG_SUCCESS = "+success);

                if (success == 1) {
                    Log.v(LOG_TAG2, "success == 1");
                    // пользователь найден
                    /*Toast.makeText(getApplicationContext(),
                            "Приглашение отправлено: " + name, Toast.LENGTH_SHORT).show();*/
                }
                else{
                    Log.d(LOG_TAG2, "success == "+success);
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




    public void onMenuBtnClick(View view) {
        Intent intent = new Intent(ContactsActivity.this, MenuActivity.class);
        startActivity(intent);
    }
}
